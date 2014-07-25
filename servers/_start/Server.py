import TraitSpace
import TraitUser
import TraitEntity


class Server(TraitSpace):

    entity_list = {}

    def register_entity(self, entity):
        self.entity_list[entity.__name__.lower()] = entity

    def create_entity(self, name):
        return self.entity_list[name.lower()]()


    entities = []
    users = []

    def __init__(self, h):
        self.register_entity(Entity)
        self.add_entity(self.create_entity("entity"))

    def add_entity(self, entity):
        self.entities.append(entity)
        for user in self.users:
            user.create_entity(entity)

    def remove_entity(self, entity):
        self.entities.remove(entity)

    def on_load(self, sql):
        print "Load!"

    def on_save(self, sql):
        sql.exec("begin")
        try:
            for entity in self.entities:
                entity.on_save(sql)
        except Exception:
            sql.exec("rollback")
        else:
            sql.exec("commit")


    def on_update(self):
        for entity in self.entities:
            entity.on_update()

    def on_join(self, user):
        self.users.append(user)
        for entity in self.entities:
            user.create_entity(entity)

    def new_user(self, username, hash):
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

class Entity(TraitEntity):

    def get_id(self):
      return 0

    def on_update(self):
        print "hello!"

    def on_destroy(self):
        pass

    def on_load(self, sql):
        st = sql.prepare("select name, value from entity_data where id='%d'" % self.get_id())
        st.step()
        while st.hasRow():

            st.step()
        st.dispose()


    def on_save(self, sql):
        pass

    def get_sprite(self):
        return ""

    def get_position(self):
        return Vector3f(0,0,0)

    def get_velocity(self):
        return Vector3f(0,0,0)