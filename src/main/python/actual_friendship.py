from predict_friendship import *


def write_real_friends(ds_all, path):
    with open(path, 'w') as f:
        friend_index = ds_all.labels.index('H_areFriends')
        for data_line in ds_all.data:
            if data_line[friend_index]:
                user1_id = data_line[0]
                user2_id = data_line[1]
                f.write(f"{user1_id} {user2_id} {data_line[friend_index]}\n")
                f.write(f"{user2_id} {user1_id} {data_line[friend_index]}\n")


if __name__ == "__main__":
    if len(sys.argv) != 3:
        print("Usage: actual_friendship.py {single_vectors_path} {pairwise_vectors_path} {output_path}")
        exit(1)

    single_path = sys.argv[1]
    pairwise_path = sys.argv[2]
    output_path = sys.argv[3]
    singles = read_csv(single_path)
    pairwise = read_csv(pairwise_path)

    combined = combined_vectors_all(pairwise, singles)
    header = combined_headers(single_path, pairwise_path)

    ds = DataSet(combined, header)
    ds = ds.split(header.index('areFriends'), start_col=2)
    write_real_friends(ds, output_path)
