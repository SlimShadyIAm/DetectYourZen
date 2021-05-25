import pprint
import random 


if __name__ == "__main__":
    with open('sources_original.txt') as f:
        sources = f.read().split("\n")
        old_sources = set(sources)
        
    with open('sources_new.txt') as f:
        sources = f.read().split("\n")
        new_sources = set(sources)
        
    sources = list(old_sources & new_sources)
    
    random_sources = random.sample(sources, 10)
    random_sources = {source: source.replace("git://", 'https://').replace(".git", "") for source in random_sources}
    pprint.pprint(random_sources)
