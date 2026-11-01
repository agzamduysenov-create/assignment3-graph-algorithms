## Kruskal vs Prim - time and MST weight

| Graph | V | E | Kruskal time (ms) | Prim time (ms) | Kruskal weight | Prim weight | Same weight | Connected |
|---|---|---|---|---|---|---|---|---|
| Small | 10 | 30 | 0.0872 | 0.0518 | 134 | 134 | yes | yes |
| Medium | 100 | 500 | 0.1584 | 0.1043 | 1153 | 1153 | yes | yes |
| Large | 1000 | 5000 | 1.0422 | 0.9788 | 12900 | 12900 | yes | yes |

## Kruskal vs Prim - operation counts

| Graph | Kruskal edge exams | Kruskal sort comparisons | Kruskal DSU finds | Kruskal DSU unions | Kruskal total ops | Prim edge exams | Prim PQ inserts | Prim PQ polls | Prim heap comparisons | Prim total ops |
|---|---|---|---|---|---|---|---|---|---|---|
| Small | 15 | 109 | 30 | 15 | 169 | 60 | 30 | 30 | 204 | 324 |
| Medium | 180 | 3839 | 360 | 180 | 4559 | 1000 | 500 | 500 | 7770 | 9770 |
| Large | 3271 | 50394 | 6542 | 3271 | 63478 | 10000 | 5000 | 5000 | 111028 | 131028 |
