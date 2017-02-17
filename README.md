# cik-lookup
This is a Java function for AWS Lambda.

It indexes the Central Index Key (CIK) reference file published by the SEC and performs a fuzzy-search against the published company names.

## Search
This is an [n-gram](https://en.wikipedia.org/wiki/N-gram) based search.
### Index
```
Name: ACME Bread Company.com
CIK: 0001437419
```
Each company name is split into tokens, and n-grams are built from the name:


```
tokens("ACME Bread Company.com")
["acme", "bread", "company", ".", "com"]
```
Tokens are split into n-grams, here's an example of n-grams with a minsize of 1 and a max of 3:
```
ngrams(["acme", "bread", "company", ".", "com"], 3)
["acm", "cme", "bre", "rea", "ead", "com", "omp", "mpa", "pan", "any", ".", "com"]
```
This is condensed to a map of each gram to the number of times it appeared in the original string:
```
{
  "acm": 1,
  "cme": 1,
  ...,
  "com": 2
}
```
This is map, along with the company's CIK are combined into a master map of `{gram: {score: cik[]}}` :
```
{
 "acm": {
    1: [1000000000, 1000000047],
    ...
 },
 "com": {
    1: [1000000047],
    2: [0000000000],
    ...
  },
  ...
}
```
```
