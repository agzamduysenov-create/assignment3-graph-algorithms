## Topological order and SCC

| Graph | V | E | Directed graph type | Topo time (ms) | Topo result | Topo edge exams | SCC time (ms) | #SCC | Largest SCC | SCC vertex visits | SCC edge exams |
|---|---|---|---|---|---|---|---|---|---|---|---|
| Small | 10 | 30 | DAG | 0.0546 | valid order | 60 | 0.0834 | 10 | 1 | 20 | 60 |
| Small | 10 | 30 | Random (cyclic) | 0.0178 | cycle detected (10 blocked) | 30 | 0.0540 | 1 | 10 | 20 | 60 |
| Medium | 100 | 500 | DAG | 0.0522 | valid order | 1000 | 0.0826 | 100 | 1 | 200 | 1000 |
| Medium | 100 | 500 | Random (cyclic) | 0.0683 | cycle detected (100 blocked) | 500 | 0.0993 | 1 | 100 | 200 | 1000 |
| Large | 1000 | 5000 | DAG | 0.2605 | valid order | 10000 | 0.6324 | 1000 | 1 | 2000 | 10000 |
| Large | 1000 | 5000 | Random (cyclic) | 0.1231 | cycle detected (990 blocked) | 5054 | 0.2970 | 18 | 983 | 2000 | 10000 |
