# cik-lookup
This is a Java function for AWS Lambda.

It indexes the Central Index Key (CIK) reference file published by the SEC and performs a fuzzy-search against the published company names.

## Search
This is an [n-gram](https://en.wikipedia.org/wiki/N-gram) based search.
### Index
```
Name: ACME Bread Company.com
CIK: 1000000000
```
```
Name: foo.com
CIK: 1000000047
```
Each company name is split into tokens, and n-grams are built from the name:


```
tokens("ACME Bread Company.com")
["acme", "bread", "company", ".", "com"]
```
Tokens are split into n-grams, here's an example of n-grams with a minsize of 1 and a max of 3:
```
ngrams(["acme", "bread", "company", ".", "com"])
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
This is map, along with the company's CIK are combined into a master map of `{gram: {score: [cik]}}`, and merged with the results for every company:
```
{
 "acm": {
    1: [1000000000],
 },
 "cme": {
    1: [1000000000],
 },
 "foo": {
    1: [1000000047],
 },
 "com": {
    1: [1000000047],
    2: [1000000000],
  },
  ".": {
    1: [1000000000, 1000000047]
    }
  }
  ...
}
```
## Query
```
query: acme.com
```
The query string is split into n-grams:
```
ngrams(tokens("acme.com"))
[".", "com"]
```
The mapping {score: [cik]} `index[gram]` is retrieved for each gram, and the sum total of each CIKs appearance in all gram indexes is computed. This result is then sorted and returned:
```
{
  name: ACME Bread Company.com,
  cik: 1000000000,
  score: 3
},
{
  name:foo.com,
  cik: 1000000047,
  score: 2
}
```