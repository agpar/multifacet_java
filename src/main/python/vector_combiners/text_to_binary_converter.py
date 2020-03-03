import csv
import numpy as np
import os
from tqdm import tqdm
from vector_combiners import TextVectorCombiner


class TextToBinaryConverter:
    def __init__(self, csv_file):
        self.csv_file = csv_file

    def convert(self, sort=True):
        file_length = TextVectorCombiner.csv_data_len(self.csv_file)

        with open(self.csv_file, 'r') as f:
            header = [s.strip() for s in f.readline().split(",")]

            reader = csv.reader(f)

            if header[0] == 'user_id':
                id_indexes = (0,)
            elif header[0] == 'user1Id':
                id_indexes = (0, 1)
            else:
                raise Exception("Can't parse user id's from csv header.")

            id_arr = np.empty((file_length, len(id_indexes)), dtype=np.int32)
            arr = np.empty((file_length, len(header) - len(id_indexes)), dtype=np.float32)

            t = tqdm(file_length)
            for i, line in enumerate(reader):
                for j, id_index in enumerate(id_indexes):
                    id_arr[i][j] = int(line[id_index])
                arr[i] = list(map(TextVectorCombiner.to_float, line[id_indexes[-1] + 1:]))
                t.update()

            # sort on the first id
            if sort:
                sorted_indexes = np.argsort(id_arr[:,0])
                id_arr = id_arr[sorted_indexes]
                arr = arr[sorted_indexes]

        output_dir = "/".join(self.csv_file.split("/")[:-1])
        output_file = self.csv_file.split("/")[-1]
        np.savez(os.path.join(output_dir, output_file.split('.')[0] + ".npz"), ids=id_arr, data=arr, header=np.array(header))
