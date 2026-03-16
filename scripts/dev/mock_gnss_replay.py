#!/usr/bin/env python3
import argparse
import time


def main() -> int:
    parser = argparse.ArgumentParser()
    parser.add_argument("--once", action="store_true")
    args = parser.parse_args()

    sample = "$GPGGA,123519,4807.038,N,01131.000,E,1,08,0.9,545.4,M,46.9,M,,*47"
    if args.once:
        print(sample)
        return 0

    while True:
        print(sample, flush=True)
        time.sleep(1)


if __name__ == "__main__":
    raise SystemExit(main())
