# anytext
Simple but flexible text parsing library. 
Combine atomic parsers to create complex text parsers.

Simple use:
```java
Path path = Paths.get(getClass().getResource("/ru/neoflex/anytext/email.json").toURI());
String grammarString = new String(Files.readAllBytes(path), "UTF8");
Grammar grammar = new Grammar(grammarString);
List<List> nodes = grammar.getEmitter("EmailList").parse(null,
        "very.unusual.\"@\\ \".unusual.com@strange.my-example.com," +
                "very.\"(),:;<>[]\".VERY.\"very@\\\\\\ \\\"very\".unusual@[192.168.0.100]");
Assert.assertThat(nodes.size(), CoreMatchers.is(2));
Assert.assertThat(nodes.get(0).size(), CoreMatchers.is(3));
```
Content of **nodes**:

| email  | name | domain |
| ------ | ---- | ------ |
|very.unusual."@ ".unusual.com@strange.my-example.com|very.unusual."@ ".unusual.com|;strange.my-example.com;|
|very."(),:;<>[]".VERY."very@\ "very".unusual@[192.168.0.100]|very."(),:;<>[]".VERY."very@\ "very".unusual|;[192.168.0.100];|


Content of grammar file:
```json
{
  "baseName": "Email",
  "parsers": [
    {"name": "Hyphen", "baseName": "eq", "args": {"value": "-"}},
    {"name": "CommAt", "baseName": "eq", "args": {"value": "@"}},
    {"name": "Space", "baseName": "eq", "args": {"value": " "}},
    {"name": "Dot", "baseName": "eq", "args": {"value": "."}},
    {"name": "Comma", "baseName": "eq", "args": {"value": ","}},
    {"name": "LSqBracket", "baseName": "eq", "args": {"value": "["}},
    {"name": "RSqBracket", "baseName": "eq", "args": {"value": "]"}},
    {"name": "BackSlash", "baseName": "eq", "args": {"value": "\\\\"}},
    {"name": "Quotation", "baseName": "eq", "args": {"value": "\\\""}},
    {"name": "Digit", "baseName": "set", "args": {"chars": "0123456789"}},
    {"name": "EnLCLetter", "baseName": "set", "args": {"chars": "abcdefghijklmnopqrstuvwxyz"}},
    {"name": "EnUCLetter", "baseName": "set", "args": {"chars": "ABCDEFGHIJKLMNOPQRSTUVWXYZ"}},
    {"name": "EnLetter", "baseName": "or", "args": {"parsers":[
      {"baseName": "EnLCLetter"},
      {"baseName": "EnUCLetter"}
    ]}},
    {"name": "EnNumLetter", "baseName": "or", "args": {"parsers":[
      {"baseName": "EnLetter"}, {"baseName": "Digit"}]}
    },
    {"name": "EnNumString", "baseName": "last", "args": {"parser": {"baseName": "repeat", "args": {"parser":
      {"baseName": "EnNumLetter"}
    }}}},
    {"name": "EmailNameLetter", "baseName": "or", "args": {"parsers":[
      {"baseName": "EnNumLetter"},
      {"baseName": "set", "args": {"chars": "!#$%&'*+-/=?^_`{|}~"}}
    ]}},
    {"name": "EmailQuotedString", "baseName": "seq", "args": {"parsers":[
      {"baseName": "Quotation"},
      {"baseName": "last", "args": {"parser": {"baseName": "repeat", "args": { "min": 0, "parser":
        {"baseName": "or", "args": {"parsers": [
          {"baseName": "EmailNameLetter"},
          {"baseName": "set", "args": {"chars": "(),:;<>@[]"}},
          {"baseName": "seq", "args": {"parsers":[
            {"baseName": "BackSlash"},
            {"baseName": "BackSlash"}
          ]}},
          {"baseName": "seq", "args": {"parsers":[
            {"baseName": "BackSlash"},
            {"baseName": "Space"}
          ]}},
          {"baseName": "seq", "args": {"parsers":[
            {"baseName": "BackSlash"},
            {"baseName": "Quotation"}
          ]}}
        ]}}
      }}}},
      {"baseName": "Quotation"}
    ]}},
    {"name": "EmailNamePart", "baseName": "or", "args": {"parsers":[
      {"baseName": "EmailQuotedString"},
      {"baseName": "last", "args": {"parser": {"baseName": "repeat", "args": { "min": 0, "parser": {"baseName": "EmailNameLetter"}}}}}
    ]}},
    {"name": "EmailName", "baseName": "seq", "args": {"parsers":[
      {"baseName": "EmailNamePart"},
      {"baseName": "option", "args": {"parser": {"baseName": "seq", "args": {"parsers": [
        {"baseName": "Dot"},
        {"baseName": "EmailName"}
      ]}}}}
    ]}},
    {"name": "HostNamePart", "baseName": "seq", "args": {"parsers":[
      {"baseName": "EnNumString"},
      {"baseName": "option", "args": {"parser": {"baseName": "seq", "args": {"parsers": [
        {"baseName": "Hyphen"},
        {"baseName": "HostNamePart"}
      ]}}}}
    ]}},
    {"name": "HostName", "baseName": "seq", "args": {"parsers":[
      {"baseName": "HostNamePart"},
      {"baseName": "option", "args": {"parser": {"baseName": "seq", "args": {"parsers": [
        {"baseName": "Dot"},
        {"baseName": "HostName"}
      ]}}}}
    ]}},
    {"name": "IPDigitsGroup", "baseName": "last", "args": {"check": "${node.asInteger() < 256}", "parser":
      {"baseName": "repeat", "args": {"min": 1, "max": 3, "parser": {"baseName": "Digit"}}}
    }},
    {"name": "IPAddress", "baseName": "seq", "args": {"parsers":[
      {"baseName": "IPDigitsGroup"}, {"baseName": "Dot"},
      {"baseName": "IPDigitsGroup"}, {"baseName": "Dot"},
      {"baseName": "IPDigitsGroup"}, {"baseName": "Dot"},
      {"baseName": "IPDigitsGroup"}
    ]}},
    {"name": "IPHostName", "baseName": "seq", "args": {"parsers":[
      {"baseName": "LSqBracket"},
      {"baseName": "IPAddress"},
      {"baseName": "RSqBracket"}
    ]}},
    {"name": "Email", "baseName": "seq", "args": {"parsers":[
      {"baseName": "EmailName", "name": "NamePart"},
      {"baseName": "CommAt"},
      {"baseName": "or", "name": "EmailDomain", "args": {"parsers":[
        {"baseName": "IPHostName"},
        {"baseName": "HostName"}
      ]}}
    ]}},
    {"name": "EmailSingle", "baseName": "seq", "args": {"parsers":[
      {"baseName": "Email"},
      {"baseName": "eos"}
    ]}},
    {"name": "EmailList", "baseName": "seq", "args": {"parsers":[
      {"baseName": "Email"},
      {"baseName": "option", "args": {"parser": {"baseName": "seq", "args": {"parsers": [
        {"baseName": "Comma"},
        {"baseName": "EmailList"}
      ]}}}},
      {"baseName": "eos"}
    ]}}
  ],
  "emitters": [
    {
      "name": "EmailList",
      "parser": "EmailList",
      "isRow": "${node.name == 'Email'}",
      "columns": [
        {"isColumn": "${node.name == 'Email'}"},
        {"isColumn": "${node.name == 'NamePart'}"},
        {"isColumn": "${node.name == 'EmailDomain'}", "value": "${';' += node.asString() += ';'}"}
      ]
    }
  ]
}
```