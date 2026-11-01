## DSU - worst case (chain of unions, then find on every element)

| n | Variant | Avg time (ms) | Find calls | Union calls | Parent pointer steps |
|---|---|---|---|---|---|
| 1000 | No optimizations | 0.9213 | 2998 | 999 | 499500 |
| 1000 | Path compression only | 0.0800 | 2998 | 999 | 1997 |
| 1000 | Union by rank only | 0.0749 | 2998 | 999 | 1997 |
| 1000 | PC + union by rank | 0.0792 | 2998 | 999 | 1997 |
| 5000 | No optimizations | 23.2670 | 14998 | 4999 | 12497500 |
| 5000 | Path compression only | 0.0516 | 14998 | 4999 | 9997 |
| 5000 | Union by rank only | 0.0497 | 14998 | 4999 | 9997 |
| 5000 | PC + union by rank | 0.0728 | 14998 | 4999 | 9997 |
| 10000 | No optimizations | 102.8380 | 29998 | 9999 | 49995000 |
| 10000 | Path compression only | 0.1328 | 29998 | 9999 | 19997 |
| 10000 | Union by rank only | 0.1229 | 29998 | 9999 | 19997 |
| 10000 | PC + union by rank | 0.0957 | 29998 | 9999 | 19997 |

## DSU - random case (n random unions, then n random finds)

| n | Variant | Avg time (ms) | Find calls | Union calls | Parent pointer steps |
|---|---|---|---|---|---|
| 1000 | No optimizations | 0.3689 | 3000 | 1000 | 95288 |
| 1000 | Path compression only | 0.2123 | 3000 | 1000 | 4118 |
| 1000 | Union by rank only | 0.1914 | 3000 | 1000 | 3351 |
| 1000 | PC + union by rank | 0.2629 | 3000 | 1000 | 2302 |
| 10000 | No optimizations | 14.0005 | 30000 | 10000 | 7817847 |
| 10000 | Path compression only | 0.6402 | 30000 | 10000 | 46063 |
| 10000 | Union by rank only | 0.5848 | 30000 | 10000 | 36295 |
| 10000 | PC + union by rank | 0.6425 | 30000 | 10000 | 23677 |
| 100000 | No optimizations | 4438.8980 | 300000 | 100000 | 784922062 |
| 100000 | Path compression only | 8.3075 | 300000 | 100000 | 522543 |
| 100000 | Union by rank only | 8.3862 | 300000 | 100000 | 383191 |
| 100000 | PC + union by rank | 6.8228 | 300000 | 100000 | 237255 |
