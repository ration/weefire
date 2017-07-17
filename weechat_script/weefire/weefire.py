"""
WeeFire is a script to relay messages from android to WeeChat through the
firebase realtime database


"""
import hashlib
import json
import os
import time
from json import JSONEncoder

import pyrebase

in_weechat = False
try:
    import weechat

    in_weechat = True
except ImportError:
    print("Warn - no weechat module")

SCRIPT_AUTHOR = "Tatu Lahtela <lahtela@iki.fi>"
SCRIPT_NAME = "weefire"
SCRIPT_VERSION = "0.1"
SCRIPT_LICENSE = "GPL3"
SCRIPT_DESC = "Relays WeeChat messages through Firebase realtime database"
SCRIPT_COMMAND = SCRIPT_NAME


def callback(method):
    """This function will take a bound method or function and make it a callback."""
    # try to create a descriptive and unique name.
    try:
        func = method.func_name
    except AttributeError:
        func = method.__name__
    try:
        im_self = method.im_self
        try:
            inst = im_self.__name__
        except AttributeError:
            try:
                inst = im_self.name
            except AttributeError:
                raise Exception("Instance %s has no __name__ attribute" % im_self)
        cls = type(im_self).__name__
        name = '_'.join((cls, inst, func))
    except AttributeError:
        # not a bound method
        name = func

    # set our callback
    import __main__
    setattr(__main__, name, method)
    return name


class GUID:
    @staticmethod
    def id(key):
        return hashlib.md5(key.encode()).hexdigest()


class Channel():
    def __init__(self, name, server, topic=""):
        self.name = name
        self.topic = topic
        self.id = GUID.id(self.name)
        self.server = server


class Channel():
    def __init__(self, name, server, topic=""):
        self.name = name
        self.topic = topic
        self.id = GUID.id(self.name)
        self.server = server

    @staticmethod
    def from_list(channels):
        return {c.id: c.__dict__ for c in channels}


class Message:
    def __init__(self, gid, text, channel, source, timestamp, server):
        self.gid = gid
        self.text = text
        self.channel = channel
        self.source = source
        self.timestamp = timestamp
        self.server = server

    @classmethod
    def escape(cls, value):
        return value.replace("#", "").replace(".", "_")


class WeeFireData:
    def __init__(self, firebase, config=None):
        assert (firebase is not None or config is not None)

        if not firebase:
            self.firebase = WeeFireData.load_firebase(config)
        else:
            self.firebase = firebase
        self.db = firebase.database()
        self._stream_callback = None

    def update_channel_list(self, channels):
        self._user_root().child("channel_list").set(Channel.from_list(channels))

    def message(self, message):
        """
        Send message to database
        :param Message message:
        """

        self._user_root().child("channels").child(GUID.id(message.channel)).push(message.__dict__)
        return True

    def listen_incoming(self, message_callback):
        self._stream_callback = message_callback
        self._user_root().child("input").stream(self._stream_handler)

    def _stream_handler(self, message):
        print(message["event"])
        print(message["path"])
        print(message["data"])
        gid = message["path"].replace("/", "")
        msg = json.loads(message["data"])
        if self._stream_callback is not None:
            self._stream_callback({gid: msg})

    def delete_incoming(self, gid):
        raise NotImplementedError

    def _user_root(self):
        # TODO user root real implementation
        return self.db.child("users").child("1")

    @staticmethod
    def load_firebase(config=None):
        if config is not None:
            return pyrebase.initialize_app(config)
        else:
            return pyrebase.initialize_app(WeeFireData.config_from_file())

    @staticmethod
    def config_from_file(file_name=os.path.dirname(os.path.realpath(__file__)) + os.sep + "config.json"):
        with open(file_name, 'r') as data:
            return json.load(data)


class WeeFireChat:
    def __init__(self, weechat, db):
        """

        :type db: WeeFireData
        """
        self.weechat = weechat
        self.init = False
        self.__name__ = "WeeFireChat"
        self.db = db

    def on_message(self, data, buffer, date, tags, displayed, highlight, prefix, message):
        channel = self.weechat.buffer_get_string(buffer, "localvar_channel")
        server = self.weechat.buffer_get_string(buffer, "localvar_server")

        timestamp = int(time.time())
        # Should gid be something else?
        msg = Message(gid=timestamp, text=message, timestamp=timestamp, source=prefix, channel=channel, server=server)
        if self.db.message(msg):
            return self.weechat.WEECHAT_RC_OK

    def find_channels(self):
        """Return list of servers and channels"""
        items = {}
        infolist = self.weechat.infolist_get('irc_server', '', '')
        # populate servers
        while self.weechat.infolist_next(infolist):
            items[self.weechat.infolist_string(infolist, 'name')] = ''

        self.weechat.infolist_free(infolist)
        channels = []

        # populate channels per server
        for server in items.keys():
            keys = []
            items[server] = ''  # init if connected but no channels
            infolist = self.weechat.infolist_get('irc_channel', '', server)
            while self.weechat.infolist_next(infolist):
                if self.weechat.infolist_integer(infolist, 'nicks_count') == 0:
                    # parted but still open in a buffer: bit hackish
                    continue
                if self.weechat.infolist_integer(infolist, 'type') == 0:
                    channels.append(Channel(name=self.weechat.infolist_string(infolist, "name"),
                                            server=server))
            self.weechat.infolist_free(infolist)
        return channels

    def iterate(self, *args):
        items = self.weechat.infolist_get(*args)
        if not items:
            yield
            return
        while self.weechat.infolist_next(items):
            yield items
        self.weechat.infolist_free(items)

    def populate_history(self):
        for buffer in self.iterate('buffer', '', ''):
            ptr = weechat.infolist_pointer(buffer, 'pointer')
            name = weechat.buffer_get_string(ptr, 'localvar_name')
            print(name)

    def bind(self):
        self.init = self.weechat.register(SCRIPT_NAME, SCRIPT_AUTHOR, SCRIPT_VERSION,
                                          SCRIPT_LICENSE, SCRIPT_DESC, "",
                                          "")
        if self.init:
            self.weechat.hook_print("", "irc_privmsg", "", 1, callback(self.on_message), "")
            channels = self.find_channels()
            self.db.update_channel_list(channels=channels)
            self.populate_history()


if __name__ == "__main__":
    if in_weechat:
        firebase = WeeFireData.load_firebase()
        # TODO config from weechat
        db = WeeFireData(firebase)
        chat = WeeFireChat(weechat, db)
        chat.bind()
