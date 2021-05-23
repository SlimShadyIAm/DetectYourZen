### Project selection

- The original sources used by Alexandru et. al are found in `sources_original.txt`. Some of the repos are removed from this list as they were removed, either due to a DMCA takedown or by the author themselves. These are listed below.

### Results
- The results from Alexandru et. al are found in `./data-original`
- The results from the re-run from "today" are in `./data-rerun`

### Memory requirements

Adjust the settings in `.sbtopts` according to how much memory you can spare

### Running analyses

```
sbt 'runMain Crawler ./data'
```
### Aggregating results

```
./src/main/python/aggregate.py --stdout ./data
```

### Removed sources

git://github.com/chrismsimpson/Metropolis.git
git://github.com/dmulholland/ivy.git
git://github.com/kamyu104/LeetCode.git
git://github.com/livid/v2ex.git
git://github.com/m4ll0k/WAScan.gitgit://github.com/Azure/azure-sdk-for-python.git

git://github.com/mwhite/resume.git
git://github.com/threerocks/studyFiles.git

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



<!-- new -->
<!-- git://github.com/owid/covid-19-data.git - NPE -->
<!-- git://github.com/Azure/azure-sdk-for-python.git - OOM -->