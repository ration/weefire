package weefire.lahtela.iki.fi.weefire.firebase

import org.junit.Test
import org.junit.Assert.assertEquals
import weefire.lahtela.iki.fi.weefire.Channel
import weefire.lahtela.iki.fi.weefire.ChatMessage

class WeeFireDatabaseTest {
    @Test
    fun readIncomingChannels() {
        val db = WeeFireDatabase()
        val data = """[{"topic": "", "server": "irc.tdc.fi", "name": "#kaapa", "id": "6bcbe460d8dc6cc8a96d92848d3dc95b"}, {"topic": "", "server": "irc.tdc.fi", "name": "#kapsi.fi", "id": "e7e871370c41206bc05df8c8b84ba510"}, {"topic": "", "server": "irc.tdc.fi", "name": "#olutopas", "id": "e4236ee881e085b4e6d46680107fe78e"}, {"topic": "", "server": "irc.tdc.fi", "name": "#kaapa.fg", "id": "61415f11e8119d7d13e0d00f90d28c37"}, {"topic": "", "server": "irc.tdc.fi", "name": "#rymistely", "id": "0474b0675d64e933681bb5ae04f03577"}, {"topic": "", "server": "irc.tdc.fi", "name": "#hei", "id": "5a5a1d803b11e3db7b9a5e3aaf05d2f3"}, {"topic": "", "server": "irc.tdc.fi", "name": "#tumumatkat", "id": "c11dcd288ab75c46f94040e997d733c5"}, {"topic": "", "server": "irc.tdc.fi", "name": "#progpower", "id": "2f1929d3c958bee15c2d7c1b71ed29c0"}, {"topic": "", "server": "irc.freenode.net", "name": "#weechat-android", "id": "a6772b4d18f49083f07d6b804d0a48ab"}]"""
        val ans : List<Channel> = db.parseList(data)
        assertEquals("irc.tdc.fi", ans[0].server)
    }
    @Test
    fun incomingMessage() {
        val db = WeeFireDatabase()
        val data = """{"text": "Ekke: juu julkiset tilastot olemas", "server": "irc.tdc.fi", "source": "murica", "gid": 1500111286, "timestamp": 1500111286, "channel": "#kapsi.fi"}"""
        val ans : ChatMessage = db.parseObject(data)
        assertEquals("murica", ans.source)
    }
}