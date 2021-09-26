# How To Zen Your Python -- experimentation and detection code

### Repository structure
#### Source selection

- The original sources used by Alexandru et. al are found in `sources_original_repaired.txt`. These were used in **Experiment 1** (the replication experiment) Some of the repos are removed from this list as they were removed, either due to a DMCA takedown or by the author themselves. These are listed below.
- Our "new" sources for **Experiment 2**, which we found by re-running `src/main/bash/get-projects.bash`, can be found in `sources_new.txt`. You can gather your own sources by running `/src/main/bash/get-projects.bash <ouputfilename>`.

- The list of repositories and commit hashes we used for **Experiment 3** can be found in the `sources_commit_20xx_xx.txt` files

#### Scripts
- The original scripts Alexandru et al. wrote are also included in this repositories
- The new scripts we wrote are:
    - `src/main/python/aggregate.py`  -- this was slightly overhauled to fix LaTeX support, and also generate tables from multiple sources and automatically add columns in one go. Instructions in `Aggregating results` section.
    - `src/main/python/random-selection-projects.py` -- This computes the overlap between the original sources and the "new" sources we collected, and displays 10 of them randomly. Can simply be run using `python3 src/main/python/random-selection-projects.py`, outputs to standard output.
    - `src/main/python/get-commits.py` -- This script uses the GitHub API to find, given a list of Git repository sources and chosen time periods, commit hashes for each time period for each of the repositories. This was used for Experiment 3. The list of repositories was taken from `random-selection-projects.py` and you can change the list on line 6 of `get-commits.py` to change this list. You can also change the ranges of dates to use on line 17. After that, running  `python3 src/main/python/get-commits.py` will create the `.txt` files needed with the sources and commit hashes.
    - `src/main/python/repair-sources.py` -- The list of repositories the original authors gave did not correspond to the repositories of the resulting data in `./data-original`. We tried to reconstruct the list just using the folder names using this script. Run using `python3 src/main/python/repair-sources.py` and it will save the repaired sources list to `sources_original_repaired.txt`.
    - `./aggregate-timeperiods.ipynb` was used to generate graphs using the data discovered in Experiment 3 in `data-commits/`. This can be run using Jupyter notebook.

The remaining script, i.e `project-stats.bash` were from the authors of the orignal paper and is not used in this project.

#### Detectors
- The original detector is in `src/main/scala/CrawlerOrig.scala`. This was unmodified from the replication package.
- The new detector, which makes use of the enhanced idiom detector analyses, is in `src/main/scala/CrawlerNew.scala`.
- The new detector with the new Git Agent, which can additionally check out commit hashes after cloning repos, can be found in `src/main/scala/CrawlerNewCommit.scala`.
- The new analyses and Git Agent are in `src/main/scala/slim`.

***NOTE***: These scripts may take several hours to run if using all ~1,000 sources. You can terminate early at any time with `CTRL+C` because stats are saved after detection on each project is finished, not at the end. And if restarting the script, it will pick up where you left off.

If you want to test on a small selection of sources, `sources_sample.txt` contains 10 repositories, so modifying `src/main/scala/CrawlerNew.scala` to use this text file would terminate after a few minutes.

Alternatively, you can add a new `.txt` file with a subset of the sources and modify the `Source.fromFile()` function call in `src/main/scala/CrawlerNew.scala` to use your new file.

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
sbt 'runMain <crawler to use> ./<destination folder to store result CSVs>'
```

A description of each crawler is given above in the `Detectors` section.

Example: 
```
sbt 'runMain CrawlerNew ./data-new'
```

As the script runs, you will see in the output the progress of the analyses: cloning the git repository, analyzing the files, and then it will move on to the next project.

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
./src/main/python/aggregate.py -d ./data-original -d ./data-rerun-repaired -d ./data-new --tex 
```

### Reproducing each of the tables in paper
Note that calculations for percentage difference and grouping were done by hand, so these results will not be included in the outputs.

- Table 2: `./src/main/python/aggregate.py -d ./data-original -d ./data-rerun-repaired  --tex`
- Table 3: `./src/main/python/aggregate.py -d ./data-rerun-repaired -d ./data-new --tex `

### Removed sources
The below repositories were removed from lists due to errors such as:
- The author deleted the repository/changed the name or username, so the link no longer works.
- Ran out of memory while trying to run the detector

#### Repaired sources list
git://github.com/AKSHAYUBHAT/DeepVideoAnalytics.git
git://github.com/Russell91/pythonpy.git
git://github.com/SirCmpwn/evilpass.git
git://github.com/alex/django-ta.git
git://github.com/dalocean/netbox.git
git://github.com/instabot-py/instabot.py.git
git://github.com/kamyu104/LeetCode.git
git://github.com/kemayo/sublime-text.git
git://github.com/kennethreitz/l.git
git://github.com/livid/v2ex.git
git://github.com/xiyouMc/WebHubBot.git


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

<!-- despite less projects, new set had 4% higher lines of code so we weighted it on projects -->
<!-- 997
952 -->


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






<!-- `git://github.com/AKSHAYUBHAT/DeepVideoAnalytics.git` -->
<!-- git://github.com/Russell91/pythonpy.git -->