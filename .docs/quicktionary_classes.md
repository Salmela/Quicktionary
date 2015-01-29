# 1. Quicktionary GUI

## 1.0. Main
The main class that processes the command line arguments and starts up the MainWindow.

## 1.1/2. Application
Move all the communication of the gui to this class and leave only gui specific stuff to the MainWindow.

## 1.1. MainWindow
The main window that communicates with backend and passes the results from backend to it’s components.
Rename the class to just Window.

## 1.2. SearchBox
The text field at the header of the window.

### Behaviour:
* When user starts typing to search box then the PageArea component is swapped with the SearchResults component.
* If the user presses enter the first item of SearchResults is showed in PageArea component.

## 1.3. SearchResults
List component that shows the search results while user is typing. The List contains array of [backend objects].

### Behaviour:
* When user clicks a search result then the SearchResults component is swapped with the page area component. And a page is queried for the search result from backend.

## 1.4. PageArea
The component that shows all information about the word.


# 2. Quicktionary Backend
Open questions: how much WordDatabase knows about context in other words can it order the search item by their relevantity?

## 2.0. Dictionary
This class glues all backend’s internal comments to single api. The main task of the Dictionary object is serve the frontends queries.

### Methods
* void **search**(String query):
 * *[Called by gui.MainWindow.actionPerformed()]*
 * Sends search query to Searcher object.
* void **setSearchResultListener**(SearchResultListener listener):
 * *[Called by gui.MainWindow.makeComponents()]*
 * Register a search result listener.
 * This call can be done only once.
* int **getResultCount**(): *[will be removed?]*
 * Returns the number of found search items.
 * This information maybe send through the SearchResultListener.
* void **getPageContent**(SearchItem item, String searchTerm):
 * Tell the history object that gui requested a page.
 * Ask the page from WordDatabase object.
* void **readDatabaseDump**(String filename):
 * Pass this call to the DBParser

## 2.1. Searcher
The search queries send by the interface are parsed and processed in this class.

## 2.2. Interface SearchResultListener
When new search results are found this listener is called. This listener is implemented by atleast gui.SearchResultList and maybe Searcher.

### Methods
* void **setSearchResults**(SearchItem items[], int count)
 * The count parameter is the total number of search results.

## 2.3. SearchItem
The Searcher object or the WordDatabase creates array of SearchItem objects and passes them to SearchResultListener. The object contains some metadata that is used by the WordDatabase object.

### Methods
* **SearchItem**(String word, String Description)
* String **getWord**()
* String **getDescription**()

## 2.3. Interface Fetcher
Fetches a page with a name and domain name. The fetcher returns only the wiki markup.

### Methods
* void **setFetchListener**(fetchListener listener)
 * The listener is called when the page is fetched
* void **fetchPage**(String title, String domain)

## 2.4. FetchListener
This interface is implemented by the wikibackends.

### Methods
* void pageFetched(String title, String text)

## 2.5. HttpFetcher
Downloads the page from wikimedia wiki and returns the wiki markup. The fetcher should use json format for the api.

example query: https://en.wiktionary.org/w/api.php?format=json&action=query&titles=Car|Halo&prop=revisions&rvprop=content

### Methods
* Same as methods of interface Fetcher.

## 2.6. WikiParser
Parses the markup used in the wikimedia projects. The class returns a tree of TextElements.

The documentation of the markup can be found at:
https://www.mediawiki.org/wiki/Help:Formatting

### Methods

* TextElement[] **parseMarkup**(String Text)

## 2.6.1. WikiParser.TextElement
Public child class of the wikiparser that contains all informatin about one wiki markup element.

## Methods

* String **getText**()
* String **getType**()
* String **getSource**()
 * returns debugging string

## 2.7. DBParser

Parses the wiktionary dump and inserts new words to WordDatabase.

The raw database files can be found from:
https://dumps.wikimedia.org/backup-index.html

### Methods

* boolean **parseDB**(String filename)
* boolean **parseDB**(fileHandle) *[maybe for the tests]*

## 2.8. WordDatabase

The WordDatabase stores a database index and word data. The index is prefix tree. All data is stored in database file and data is loaded to the application when neaded.

## 2.9. [Interface] WikiBackend

This object(s) reads the TextElement array and returns some structured record. The record is serialized and send to the WordDatabase object.
