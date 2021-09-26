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

ignore_list = [
    "data-original/AKSHAYUBHAT_DeepVideoAnalytics",
    "data-original/Russell91_pythonpy",
    "data-original/SirCmpwn_evilpass",
    "data-original/alex_django-ta",
    "data-original/dalocean_netbox",
    "data-original/instabot_py/instabot.py",
    "data-original/kamyu104_LeetCode",
    "data-original/kemayo_sublime-text",
    "data-original/kennethreitz_l",
    "data-original/livid_v2ex",
    "data-original/xiyouMc_WebHubBot",
    "data-original/PressLabsfs",
    "data-original/anishathalye-remote-dropbox",
    "data-original/arc90-sweep",
    "data-original/donnemartinsome",
    "data-original/gelstudiosfiti",
    "data-original/git-cola-cola",
    "data-original/sdg-mitless"
]

argp = argparse.ArgumentParser()
argp.add_argument('-d',
                   help="Path containing project directories", nargs="+", action="append")
argp.add_argument('--tex', dest='tex', action='store_true')
argp.add_argument('--stdout', dest='stdout', action='store_true')
argp.add_argument('--weighted', dest='weighted', action='store_true')
argp.add_argument('--diffy', dest='diffy', action='store_true')
argp.set_defaults(tex=False, stdout=False)
args = argp.parse_args()

all_projects_initial = []
headers = set()
skipHeaders = 8
max_projects = -1

for datadir in args.d:
    allData = OrderedDict()
    datadir = datadir[0]
    idiomStats = OrderedDict()

    projects = sorted(glob.glob(os.path.join(datadir, '*')))
    if len(projects) == 0:
         raise FileNotFoundError(f"{datadir} was not found! Exiting...")
    
    if len(projects) > max_projects:
        max_projects = len(projects)
        
    for project in projects:
        if project in ignore_list or project in [f"./{i}" for i in ignore_list]:
            continue
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
    all_projects_initial.append((idiomStats, len(projects)))

all_projects = []
for idiomStats, numProjects in all_projects_initial:
    all_projects.append((idiomStats, max_projects / numProjects))
            
if args.tex:
    print("\\centering")
    if args.diffy:
        print("\\begin{tabular} { c || " + ' || '.join(['c | c' for _ in range(len(all_projects) + 2)]) + "} \\\\")
    else:
        print("\\begin{tabular} { c || " + ' || '.join(['c | c' for _ in range(len(all_projects))]) + "} \\\\")
    headers = "Idioms"
    
    for i, header in enumerate(args.d):
        headers += " & \multicolumn{2}{c||}{" + header[0] + "}"
    
    if args.diffy:
        headers += " & \multicolumn{2}{c||}{Percentage difference}"
    
    print(headers + " \\\\")
    
    # line = (f"\cline")
    yes = ""
    for i in range(len(all_projects)):
        i += 1
        i *= 2
        
        yes += "\cline{" + str(i) + "-" + str(i+1) + "} "
        
    if args.diffy:
        yes += "\cline{6-7} "
    for _ in range(len(all_projects)):
        yes += (f" & Projects & Use Count")
    if args.diffy:
        yes += " & Difference Projects & Difference Use Count"
    yes += " \\\\ [0.5ex]"
    print(yes)
    
    print("\hline\hline")
    for name, meta in idioms.items():
        found = False
        for idiomStats, _ in all_projects:
            stats = idiomStats.get(meta[2])
            if stats is not None:
                found = True
                break
            
        if not found:
            continue
        
        line = "\\textbf{" + name + "}"
        if args.diffy:
            idiomStats_before, _ = all_projects[0]
            idiomStats_after, _ = all_projects[1]
            line += " & "
            stats_before = idiomStats_before.get(meta[2])
            do_diffy = True
            if stats_before is None:
                    line += ("\\textbf{---} & \\textbf{---}")
                    do_diffy = False
            else:
                line += ('\\textbf{' + str(stats_before["present"]) + '} & \\textbf{' + str(stats_before["count"]) + '}')
            line += " & "

            stats_after = idiomStats_after.get(meta[2])
            if stats_after is None:
                    line += ("\\textbf{---} & \\textbf{---}")
                    do_diffy = False
            else:
               line += ('\\textbf{' + str(stats_after["present"]) + '} & \\textbf{' + str(stats_after["count"]) + '}')

            line += " & "
            if do_diffy:
                diff_projects = round((int(stats_after["present"]) - int(stats_before["present"])) / int(stats_before["present"]) * 100, 2)
                if diff_projects < 0:
                    diff_projects = "\cellcolor{red!25}{" + str(-1*diff_projects) + "}"
                else:
                    diff_projects = "\cellcolor{green!25}{" + str(diff_projects) + "}"
                
                diff_count = round((int(stats_after["count"]) - int(stats_before["count"])) / int(stats_before["count"]) * 100, 2)
                if diff_count < 0:
                    diff_count = "\cellcolor{red!25}{" + str(-1*diff_count) + "}"
                else:
                    diff_count = "\cellcolor{green!25}{" + str(diff_count) + "}"
                line += ("\\textbf{" + str(diff_projects) + "\%}& \\textbf{" + str(diff_count) + "\%}")
            else:
                line += ("\\textbf{---} & \\textbf{---}")
        else:
            for idiomStats, weight in all_projects:
                line += " & "
                stats = idiomStats.get(meta[2])
                if stats is None:
                    line += ("\\textbf{---} & \\textbf{---}")
                else:
                    # Idioms without a description
                    if args.weighted:
                        line += ('\\textbf{' + str(stats["present"]*weight) + '} & \\textbf{' + str(stats["count"]*weight) + '}')
                    else:
                        line += ('\\textbf{' + str(stats["present"]) + '} & \\textbf{' + str(stats["count"]) + '}')
        line += " \\\\"
        print(line)
    print("\end{tabular}")
if args.stdout:
    for key, values in idiomStats.items():
        print(f"{key}")
        for k, v in values.items():
            print(f"    {k}: {v}")
        print("---")

