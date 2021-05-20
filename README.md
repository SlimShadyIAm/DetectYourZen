### Project selection

The projects to be analyzed are listed in `python.txt`, one per line.

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

