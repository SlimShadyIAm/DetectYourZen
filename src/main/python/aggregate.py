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
    ("heapq",          [1, 1, "counters.heapq", "heapq"]),
    ("collections.Counter",        [0, 1, "counters.Counter", "counter"]),
    ("pprint.pprint",              [0, 1, "counters.pprint", "pprint"]),
    ("@classmethod",               [0, 1, "counters.classmethod", "classmethod"]),
    ("@staticmethod",              [0, 1, "counters.staticmethod", "staticmethod"]),
    ("@property",              [0, 1, "counters.property", "property"]),
    ("zip",                        [1, 1, "counters.zip", "zip"]),
    ("itertools",                  [1, 1, "itertools", "itertools"]),
    ("functools.total\\_ordering,", [0, 1, "counters.total_ordering", "ordering"]),
    ("\_\_repr\_\_ and \_\_str\_\_", [0, 1, "ReprStrs.level1", "reprstr"]),
    ("pprint", [0, 1, "counters.pprint", "pprint"]),
    ("format", [0, 1, "counters.format", "format"]),
    ("join", [0, 1, "counters.join", "join"]),
])
itertools = [
  "counters.izip_longest",
  "counters.zip_longest",
  "counters.starmap",
  "counters.tes",
  "counters.groupby",
]

argp = argparse.ArgumentParser()
argp.add_argument('-d',
                   help="Path containing project directories", nargs="+", action="append")
argp.add_argument('--tex', dest='tex', action='store_true')
argp.add_argument('--stdout', dest='stdout', action='store_true')
argp.set_defaults(tex=False, stdout=False)
args = argp.parse_args()

all_projects = []
headers = set()
skipHeaders = 8

for datadir in args.d:
    allData = OrderedDict()
    datadir = datadir[0]
    idiomStats = OrderedDict()
    for project in sorted(glob.glob(os.path.join(datadir, '*'))):
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
    all_projects.append(idiomStats)
if args.tex:
    if len(all_projects) == 0:
        idiomStats = all_projects[0]
        print("\\begin{center}")
        print("\\begin{tabular}{c | c | c} \\\\")
        print("Idiom & Projects & Use Count \\\\ [0.5ex]")
        print("\hline\hline")
        for name, meta in idioms.items():
            stats = idiomStats.get(meta[2])

            if stats is not None and stats.get("present"):
                # Idioms without a description

                print("\\textbf{%s} & \\textbf{%d} & \\textbf{%d} \\\\ " %
                (name,
                stats["present"],
                stats["count"],
                # meta[3])
                )
                )
        print("\end{tabular}")
        print("\end{center}")
    else:
        print("\\begin{center}")
        print("\\begin{tabular} { c || " + ' || '.join(['c | c' for _ in range(len(all_projects))]) + "} \\\\")
        headers = "Idioms"
        
        for i, header in enumerate(args.d):
            headers += " & \multicolumn{2}{c}{" + header[0] + "}"
        
        print(headers + " \\\\")
        
        # line = (f"\cline")
        yes = ""
        for i in range(len(all_projects)):
            i += 1
            i *= 2
            
            yes += "\cline{" + str(i) + "-" + str(i+1) + "} "
            
        for _ in range(len(all_projects)):
            yes += (f" & Projects & Use Count")
        yes += " \\\\ [0.5ex]"
        print(yes)
        
        print("\hline\hline")
        for name, meta in idioms.items():
            stats = all_projects[0].get(meta[2])
            if stats is None:
                continue
            line = "\\textbf{" + name + "}"
            for idiomStats in all_projects:
                line += " & "
                stats = idiomStats.get(meta[2])
                # Idioms without a description
                line += ("\\textbf{%d} & \\textbf{%d}" %
                    (stats["present"],
                    stats["count"])
                    )
            line += " \\\\"
            print(line)
        print("\end{tabular}")
        print("\end{center}")

# if args.stdout:
#     import pprint
#     pp = pprint.PrettyPrinter(depth=6)
#     pp.pprint(idiomStats)

