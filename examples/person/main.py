#!/usr/bin/env python3
#main.py
import person

def main():
    pp = person.PersonParser()
    joe = pp.getPerson('testPerson.txt')
    print('Before Change:')
    print(joe)
    # Joe's file hasn't been updated since his birthday, so we update the age field:
    joe.age = 19
    print('After Change:')
    print(joe)
    return joe


if __name__ == "__main__":
    obj = main()
