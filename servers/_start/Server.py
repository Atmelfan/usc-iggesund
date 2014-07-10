import TraitSpace
import TraitUser


class Server(TraitSpace):

    def __init__(self, h):
        print(h)

    def on_load(self):
        print "Load!"

    def on_update(self):
        print "hello world!"

    def on_join(self, username, hash):
        return User(username)


class User(TraitUser):
    cmd = ""

    def __init__(self, username):
        self.username = username

    def event_keyboard(self, key, char, state):
        if state:
            print("Key %s pressed!" % char)

        if key == 28:
            print("Command: %s" % self.cmd)

    def event_mouse(self, button, state, x, y):
        if state:
            print("Button %i pressed at %i,%i!" % (button, x, y))
