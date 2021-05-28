import os

folder_set = set()

dirs = sorted(os.listdir('data-original/'))
for dir_ in dirs:
    dir_ = dir_.split("_")
    
    if len(dir_) == 1:
        continue
        
    else:
        owner = dir_[0]
        repo = dir_[1]
        
        url = f"git://github.com/{owner}/{repo}.git"
        folder_set.add(url.strip())

print(f"Original length: {len(dirs)}")
print(f"New length: {len(folder_set)}")
folder_sources = "\n".join(sorted(list(folder_set)))        
with open('sources_original_repaired.txt', 'w') as f:
    f.write(folder_sources)
