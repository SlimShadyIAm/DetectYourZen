import pprint
import random 


if __name__ == "__main__":
    with open('sources_original.txt') as f:
        sources = f.read().split("\n")
        sources = list(set(sources))
        
        new_sources = random.sample(sources, 10)
        new_sources = {source: source.replace("git://", 'https://').replace(".git", "") for source in new_sources}
        pprint.pprint(new_sources)