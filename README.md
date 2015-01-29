# Quicktionary WIP
This app is made for a university course. The app is divided into two parts; a backend library and a gui program. The whole project is implemented using Java and as a gui toolkit the Swing package is used. The app enables you to quickly find words you want to get a translation for. The app is capable of showing search results while you are typing.

The backend supports currently only Finnish and English wiktionary. Adding support for additional languages isn't easy, because each wiktionary has their own formatting and so requires a custom parser. Backend library contains a word database that contains every word in dictionary. Every time the user queries a specific word, the backend will fetch page for that word from wiktionary.
