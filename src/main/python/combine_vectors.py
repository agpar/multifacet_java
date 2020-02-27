import argparse

from vector_combiners.combined_vector_saver import CombinedVectorSaver


def run(single_path, pairwise_path, output_path):
    cvs = CombinedVectorSaver(single_path, pairwise_path)
    cvs.save(output_path)


if __name__ == '__main__':
    parser = argparse.ArgumentParser(description='Combine single and pairwise vectors.')
    parser.add_argument('single_path',  type=str,
                        help='Path to single-agent feature csv.')
    parser.add_argument('pairwise_path', type=str,
                        help='Path to pairwise-agent feature csv.')
    parser.add_argument('output_path',  type=str,
                        help="Path to a file where output clusters are written.")
    args = parser.parse_args()
    run(**vars(args))
