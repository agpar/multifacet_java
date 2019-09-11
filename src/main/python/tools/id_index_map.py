"""
Map strings to ascending ints to simplify transformation of csv to in-memory matrix
"""


class IDIndexMap:
    def __init__(self):
        self.map = {}
        self._next_index = 0

    def get_int(self, item: str):
        if not isinstance(item, str):
            raise Exception("item must be a string.")
        if item in self.map:
            return self.map[item]
        else:
            index = self._next_index
            self._next_index += 1
            self.map[item] = index
            self.map[index] = item
            return index

    def get_str(self, item : int):
        if not isinstance(item, int):
            raise Exception("item must be an int.")
        if item in self.map:
            return self.map[item]
        else:
            raise Exception(f"No string is assigned to {item}")