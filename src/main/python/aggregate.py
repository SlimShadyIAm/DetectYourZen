#!/usr/bin/env python3
# -*- coding: utf-8 -*-

import argparse
import csv
import sys
import os
import glob
import math
import statistics
import string
from collections import OrderedDict

checkmark = "\checkmark"
tag_perf = r'\tagperf'
tag_read = r'\tagread'
idioms = OrderedDict([
    ("List comprehension",         [1, 1, "counters.ListComp", "listcomp"]),
    ("Dict comprehension",         [1, 1, "counters.DictCompargs", "dictcomp"]),
    ("Generator expression",       [1, 1, "counters.GeneratorExpargs", "genexp"]),
    ("Decorator",                  [0, 1, "counters.Namedecorator_list", "decorator"]),
    ("Simple magic methods",       [0, 0, "MagicMethods.level1", None]),
    ("Intermediate magic methods", [0, 0, "MagicMethods.level2", None]),
    ("Advanced magic methods",     [0, 0, "MagicMethods.level3", "magic"]),
    ("finally",                    [0, 1, "counters.Exprfinalbody", "finally"]),
    ("with",                       [0, 1, "counters.Withbody", "with"]),
    ("enumerate",                  [0, 1, "counters.enumerate", "enumerate"]),
    ("yield",                      [1, 0, "counters.Yield", "yield"]),
    ("lambda",                     [1, 0, "counters.Lambdaargs", "lambda"]),
    ("collections.defaultdict",    [1, 1, "counters.defaultdict", "defaultdict"]),
    ("collections.namedtuple",     [0, 1, "counters.namedtuple", "namedtuple"]),
    ("collections.deque",          [1, 1, "counters.deque", "deque"]),
    ("collections.Counter",        [0, 1, "counters.Counter", "counter"]),
    ("@classmethod",               [0, 1, "counters.classmethod", "classmethod"]),
    ("@staticmethod",              [0, 1, "counters.staticmethod", "staticmethod"]),
    ("zip",                        [1, 1, "counters.zip", "zip"]),
    ("itertools",                  [1, 1, "itertools", "itertools"]),
    ("functools.total\\_ordering,", [0, 1, "counters.total_ordering", "ordering"]),
])
itertools = [
  "counters.izip_longest",
  "counters.zip_longest",
  "counters.starmap",
  "counters.tes",
  "counters.groupby",
]

argp = argparse.ArgumentParser()
argp.add_argument('datadir', type=str,
                   help="Path containing project directories")
argp.add_argument('--tex', dest='tex', action='store_true')
argp.add_argument('--stdout', dest='stdout', action='store_true')
argp.set_defaults(tex=False, stdout=False)
args = argp.parse_args()

allData = OrderedDict()
idiomStats = OrderedDict()
headers = set()
skipHeaders = 8

for project in sorted(glob.glob(os.path.join(args.datadir, '*'))):
    datafile = os.path.join(project, "global.csv")
    if os.path.exists(datafile):
        with open(datafile, 'r') as csv_in:
            pName = os.path.basename(project)
            csvreader = csv.reader(csv_in, delimiter=',')
            headerRow = next(csvreader)[skipHeaders:]
            dataRow = next(csvreader)
            data = {}
            for i, header in enumerate(headerRow):
                headers.add(header)
                data[header] = dataRow[i+skipHeaders]
            allData[pName] = data

headers.add("itertools")
for header in sorted(headers):
    stats = {"present": 0, "count": 0}
    for project, data in sorted(allData.items()):
        # add up itertools stats
        data['itertools'] = 0
        for tool in itertools:
            if tool in data:
                data['itertools'] += int(data[tool])
        # aggregate stats
        if header in data and int(data[header]) > 0:
            stats["present"] += 1
            stats["count"] += int(data[header])
    idiomStats[header] = stats

if args.tex:
    for name, meta in idioms.items():
        stats = idiomStats[meta[2]]
        # Idioms without a description
        if not meta[3]:
            print("\\textbf{%s} %s %s & \\np{%d} & \\np{%d} \\\\" %
              (name,
              tag_perf if meta[0] else "",
              tag_read if meta[1] else "",
              stats["present"],
              stats["count"])
            )
        # All others
        else:
            print("\\textbf{%s} %s %s & \\np{%d} & \\np{%d} \\\\ \\multicolumn{3}{p{8cm}}{\idiom{%s}\\vspace{1mm}} \\\\" %
              (name,
              tag_perf if meta[0] else "",
              tag_read if meta[1] else "",
              stats["present"],
              stats["count"],
              meta[3])
            )

if args.stdout:
    import pprint
    pp = pprint.PrettyPrinter(depth=6)
    pp.pprint(idiomStats)

