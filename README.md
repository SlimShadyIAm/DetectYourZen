# How To Zen Your Python -- experimentation and detection code

### Repository structure
#### Source selection

- The original sources used by Alexandru et. al are found in `sources_original_repaired.txt`. These were used in **Experiment 1** (the replication experiment) Some of the repos are removed from this list as they were removed, either due to a DMCA takedown or by the author themselves. These are listed below.
- Our "new" sources for **Experiment 2**, which we found by re-running `src/bash/get-projects.bash`, can be found in `sources_new.txt`
- The list of repositories and commit hashes we used for **Experiment 3** can be found in the `sources_commit_20xx_xx.txt` files

#### Scripts
- The original scripts Alexandru et al. wrote are also included in this repositories
- The new scripts we wrote are:
    - `src/main/python/aggregate.py`  -- this was slightly overhauled to fix LaTeX support, and also generate tables from multiple sources and automatically add columns in one go.
    - `src/main/python/random-selection-projects.py` -- This computes the overlap between the original sources and the "new" sources we collected, and displays 10 of them randomly
    - `src/main/python/get-commits.py` -- This script uses the GitHub API to find, given a list of Git repository sources and chosen time periods, commit hashes for each time period for each of the repositories. This was used for Experiment 3.
    - `src/main/python/repair-sources.py` -- The list of repositories the original authors gave did not correspond to the repositories of the resulting data in `./data-original`. We tried to reconstruct the list just using the folder names using this script.
    - `./aggregate-timeperiods.ipynb` was used to generate graphs using the data discovered in Experiment 3 in `data-commits/`

#### Detectors
- The original detector is in `src/main/scala/CrawlerOrig.scala`. This was unmodified from the replication package.
- The new detector, which makes use of the enhanced idiom detector analyses, is in `src/main/scala/CrawlerNew.scala`.
- The new detector with the new Git Agent, which can additionally check out commit hashes after cloning repos, can be found in `src/main/scala/CrawlerNewCommit.scala`.
- The new analyses and Git Agent are in `src/main/scala/slim`.

### Results
- The results from Alexandru et. al are found in `./data-original`
- The results from the re-run using the same sources from "today" are in `./data-rerun-repaired`
- The results from the "new" sources are in  `./data-new`
- The results from the commit experiment are in `./data-commits`

### Memory requirements

Adjust the settings in `.sbtopts` according to how much memory you can spare

### Running analyses
Changing the source file (list of repositories to use as sources) can be done by modifying the `projects` variable in the appropriate Crawler file in `src/main/scala/`

To run the analyses:

```
sbt 'runMain <crawler to use> ./<destination folder>'
```

Example: 
```
sbt 'runMain CrawlerNew ./data-new'
```

### Aggregating results

This will aggregate the results from the CSV files in `./data-new` and output to console
```
./src/main/python/aggregate.py -d ./data-new --stdout 
```

You can also generate a LaTeX table:
```
./src/main/python/aggregate.py -d ./data-new --tex 
```

Or combine multiple sources of data into one table:
```
./src/main/python/aggregate.py -d ./data-original -d ./data-repaired -d ./data-new --tex 
```

### Removed sources
The below repositories were removed from lists due to errors such as:
- The author deleted the repository/changed the name or username, so the link no longer works.
- Ran out of memory while trying to run the detector

#### Repaired sources list
`git://github.com/AKSHAYUBHAT/DeepVideoAnalytics.git`
`git://github.com/Netflix/security.git`
`git://github.com/Newmu/dcgan.git`
`git://github.com/Rip-Rip/clang.git`
`git://github.com/Russell91/pythonpy.git`
`git://github.com/SirCmpwn/evilpass.git`
`git://github.com/Uberi/speech.git`
`git://github.com/ageitgey/face.git`
`git://github.com/alex/django-ta.git`

#### Incorrect "original" sources list

`git://github.com/chrismsimpson/Metropolis.git`
`git://github.com/dmulholland/ivy.git`
`git://github.com/kamyu104/LeetCode.git`
`git://github.com/livid/v2ex.git`
`git://github.com/m4ll0k/WAScan.gitgit://github.com/Azure/azure-sdk-for-python.git`
`git://github.com/mwhite/resume.git`
`git://github.com/threerocks/studyFiles.git`

#### "new" sources
`git://github.com/owid/covid-19-data.git`
`git://github.com/Azure/azure-sdk-for-python.git`


<!-- error files -->
<!-- git://github.com/abatchy17/WindowsExploits.git -->
<!-- git://github.com/ckan/ckan.git -->
<!-- git://github.com/255BITS/HyperGAN.git -->
<!-- git://github.com/ansible/ansible.git -->
<!-- git://github.com/apachecn/MachineLearning.git -->
<!-- git://github.com/AppScale/appscale.git -->
<!-- git://github.com/django-nonrel/mongodb-engine.git -->
<!-- git://github.com/deis/deis.git -->
<!-- git://github.com/datafolklabs/cement.git -->
<!-- git://github.com/dae/anki.git -->
<!-- git://github.com/cython/cython.git -->
<!-- git://github.com/ctfs/write-ups-2014.git -->
<!-- git://github.com/cs109/content.git -->
<!-- git://github.com/crossbario/crossbar.git -->
<!-- git://github.com/crossbario/autobahn-python.git -->
<!--  git://github.com/coffeehb/Some-PoC-oR-ExP.git -->




