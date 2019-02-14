#!/usr/bin/env python3
#main.py
import person

def main():
	pp = person.PersonParser()
	return pp.getPerson('testPerson.txt')


if __name__ == "__main__":
	obj = main()
	print(obj)

