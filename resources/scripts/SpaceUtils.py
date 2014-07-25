import TraitSpace
entity_dict = {}


def register_entity(name, entity):
    entity_dict[name.lower()] = entity


def create_entity(name):
    return entity_dict[name.lower()]()


class BaseSpace(TraitSpace):
    pass