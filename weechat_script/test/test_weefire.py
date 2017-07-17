import json
import unittest
from unittest import TestCase
from unittest.mock import Mock

from weefire.weefire import WeeFireData, WeeFireChat, Message, GUID


class MessageTest(TestCase):
    def test_sanitize(self):
        msg = Message(gid="123", text="Hello, World", timestamp="34234", source="#kaapa", channel="#kaapa",
                      server="server")
        self.assertEqual("kaapa", Message.escape(msg.channel))


class WeeFireDataTest(TestCase):
    def setUp(self):
        self.base = Mock()

    def test_listen_incoming(self):
        self.base.database.side_effect = lambda *args: self.base
        fire = WeeFireData(firebase=self.base,
                           config=Mock())
        self.base.child.side_effect = lambda *args: self.base

        channel = "#mychannel"
        self.handler = None
        path_id = "xyz"
        data = {"message": "hello",
                "channel": channel}
        msg = {"data": json.dumps(data),
               "event": "put",
               "path": path_id}

        def stream_handler(*args):
            self.handler = args[0]

        def incoming(data):
            """
            :type data: dict
            """
            for key, value in data.items():
                self.assertEqual(path_id, key)
                self.assertEqual("hello", value["message"])

        self.base.stream.side_effect = stream_handler
        fire.listen_incoming(incoming)
        self.handler(msg)


class WeeFireChatTest(TestCase):
    def test_bind(self):
        weechat = Mock()
        weechat.register.return_value = False
        db = Mock()
        wf = WeeFireChat(weechat, db)
        wf.bind()
        weechat.register.assert_called()

    def test_privmsg(self):
        weefire = Mock()
        weefire.buffer_get_string.return_value = "#channel"
        db = Mock()
        wf = WeeFireChat(weefire, db)

        wf.on_message(None, None,
                      None, None,
                      None, False,
                      "Username", "Message")

    def test_find_channels(self):
        used_keys = ['irc_channel', 'irc_server']

        def infolist_get(*args):
            self.assertTrue(args[0] in used_keys)
            return args[0]

        names = ["#channel1", "#channel2", "#channel3", "server"]

        def infolist_integer(*args):
            if args[1] == 'type':
                return 0
            return None

        def infolist_string(*args):
            if args[1] == 'name':
                return names.pop()
            return ''

        weechat = Mock()
        weechat.infolist_get.side_effect = infolist_get
        weechat.infolist_next.side_effect = [True, False, True, True, True, False]  # server call + channel list
        weechat.infolist_integer.side_effect = infolist_integer
        weechat.infolist_string.side_effect = infolist_string

        db = Mock()
        wf = WeeFireChat(weechat, db)
        gid = GUID.id('#channel1')

        channels = wf.find_channels()
        self.assertTrue(any(x.id == gid for x in channels))
        for key in used_keys:
            weechat.infolist_free.assert_any_call(key)


if __name__ == '__main__':
    unittest.main()
