import TraitSpace
import TraitUser
import TraitEntity


class Server(TraitSpace):
    users = []
    settings = {}

    def __init__(self, h):
        pass

    def on_load(self, sql):
        sql.exec("create table if not exists settings(name TEXT PRIMARY KEY, value);")
        statement = sql.prepare("select * from settings;")
        while statement.step():
            key = statement.columnString(0)
            value = statement.columnString(1)
            self.settings[key] = value
            print("Setting '%s'='%s'" % (key, value))
        statement.dispose()
        self.settings["first_play"] = '0'


    def on_save(self, sql):
        sql.exec("begin")
        statement = sql.prepare("insert or replace into settings(name, value) values (?, ?);")
        for key, value in self.settings.items():
            print("Executing 'insert or replace into settings(name, value) values (%s, %s);'" % (key, value))
            statement.bind(1, key)
            statement.bind(2, value)
            statement.step()
            statement.reset()
        sql.exec("commit")
    def on_update(self):
        pass

    def on_join(self, user):
        self.users.append(user)
        user.println("""USER@USCI-IGGESUND$ lst
    //USC IGGESUND - MAIN MENU
    \033[31m$CAMPAIGN
        Play campaign
    \033[31m$SERVERS
        Play on a server
    \033[31m$SETTINGS
        Open settings menu
    \033[31m$WEBSITE
        Go to usci.gpa-robotics.com
        """)

    def new_user(self, username, hash):
        return User(username)


class User(TraitUser):
    cmd = ""

    def __init__(self, username):
        self.username = username

    def event_keyboard(self, key, char, state):
        char = char.replace("[^A-Za-z0-9 !\"#%&()=@{}/\\*'?.:,;\\-+_^\n\r\b\t\\[\\]]", "")
        if state:
            self.println(char)
            self.cmd += char


    def process(self, s):
        print("Processing command: %s" % s)


    def event_mouse(self, button, state, x, y):
        if state:
            print("Button %i pressed at %i,%i!" % (button, x, y))
