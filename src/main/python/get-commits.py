import requests
from collections import defaultdict
from dotenv import load_dotenv 
import os

repos = ["git://github.com/Rapptz/discord.py.git", \
        "git://github.com/matplotlib/matplotlib.git", \
        "git://github.com/kubernetes-sigs/kubespray.git", \
        "git://github.com/dask/dask.git", \
        "git://github.com/netbox-community/netbox.git", \
        "git://github.com/scikit-image/scikit-image.git", \
        "git://github.com/conan-io/conan.git", \
        "git://github.com/sympy/sympy.git", \
        "https://github.com/spesmilo/electrum", \
]
times = [('2021-04-31', '2021-05-31'), 
         ('2020-10-31', '2020-11-31'), 
         ('2020-04-31', '2020-05-31'),
         ('2019-10-31', '2019-11-31'), 
         ('2019-04-31', '2019-05-31'),
         ('2018-10-31', '2018-11-31'), 
         ('2018-04-31', '2018-05-31'),
         ]
time_period_to_commits = defaultdict(list)

def retrieve_commits(url, session):
    parts = url.split("/")
    owner = parts[-2]
    repo = parts[-1].replace(".git", "")
    
    for since, until in times:
        period = until.split("-")
        period = f"{period[0]}-{period[1]}"
        
        
        res = session.get(f"https://api.github.com/repos/{owner}/{repo}/commits?since={since}&until={until}")
        if res.status_code != 200:
            raise Exception(f"An error occured!\n {res.json()}")
        
        res = res.json()
        
        if len(res) == 0:
            print(f"Couldn't find any commits for time period {period}")
            continue
        
        print(f"Found commit for time period {period}")
        time_period_to_commits[period].append(f"{url} {res[-1].get('sha')}")
    
if __name__ == "__main__":
    print("Starting!")
    print()
    print()
    
    load_dotenv()
    token = os.environ.get("GITHUB_PAT")
    
    with requests.Session() as session:
        session.auth = ('SlimShadyIAm', token)
        for i, repo in enumerate(repos):
            print(f"Repo {i+1}: {repo}")
            retrieve_commits(repo, session)
            print("Done!")
            print("-------")
        
    for period in time_period_to_commits:
        with open(f"sources_commit-{period}.txt", 'w') as f:
            f.write("\n".join(time_period_to_commits.get(period)))