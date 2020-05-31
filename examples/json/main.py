#!/usr/bin/env python3
#main.py
import json

def main():
    jp = json.JSONParser()
    testJson = jp.getJsonObj('testObj.json')
#    testJson = jp.getJsonObj('small.json')
    print(testJson)
    return testJson

if __name__ == "__main__":
    obj = main()
