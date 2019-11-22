import math


class LimitedReadFile:
    """A file that only allows you to read a limited number of lines."""

    def __init__(self, f, limit):
        self.f = f
        self.limit = limit
        self.read = 0

    def readline(self):
        if self.read < self.limit:
            self.read += 1
            return self.f.readline()
        else:
            return ''

    def close(self):
        self.f.close()

    def __next__(self):
        line = self.readline()
        if line is not '':
            return line
        else:
            raise StopIteration

    def __iter__(self):
        return self


def find_split_points(path, file_line_len, num):
    partition_len = math.ceil(file_line_len / num)
    i, j = 1, 1
    seek_points = [0]
    with open(path, 'r') as f:
        line = f.readline()
        while line:
            if i == partition_len and j < file_line_len:
                seek_points.append(f.tell())
                i = 0
            line = f.readline()
            i += 1
            j += 1
    return partition_len, seek_points


def split_file(path, line_len, num):
    """Given a path, return num file descriptors, each pointing to
    a different partition of the file.

    Essentially gives the illusion that the file pointed to is actually
    num different files.
    """
    partition_len, seek_points = find_split_points(path, line_len, num)
    limited_files = []
    for point in seek_points:
        f = open(path, 'r')
        f.seek(point, 0)
        limited_files.append(LimitedReadFile(f, partition_len))
    limited_files[-1].limit = math.inf
    return limited_files



