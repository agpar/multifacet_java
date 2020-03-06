from vector_combiners.text_to_binary_converter import TextToBinaryConverter
import argparse


def run(path):
    tbc = TextToBinaryConverter(path)
    tbc.convert()


if __name__ == '__main__':
    parser = argparse.ArgumentParser(description='Convert a csv file to a numpy binary')
    parser.add_argument('path', type=str, help='Path to feature csv.')
    args = parser.parse_args()
    run(**vars(args))





