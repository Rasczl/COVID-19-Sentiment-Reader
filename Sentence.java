import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Properties;
import org.ejml.simple.SimpleMatrix;
import edu.stanford.nlp.ling.CoreAnnotations;
import edu.stanford.nlp.neural.rnn.RNNCoreAnnotations;
import edu.stanford.nlp.pipeline.Annotation;
import edu.stanford.nlp.pipeline.StanfordCoreNLP;
import edu.stanford.nlp.sentiment.SentimentCoreAnnotations;
import edu.stanford.nlp.sentiment.SentimentCoreAnnotations.SentimentAnnotatedTree;
import edu.stanford.nlp.trees.Tree;
import edu.stanford.nlp.util.CoreMap;
import java.util.Date;

public class Sentence {

	private String text;
	private String author;
	private String timestamp;

	public Sentence(String text, String author, String timestamp) {

		this.text = text;
		this.author = author;
		this.timestamp = timestamp;

	}
		public int getSentiment() {
		String tweet = this.text;
		Properties props = new Properties();
		props.setProperty("annotators", "tokenize, ssplit, pos, parse, sentiment");
		StanfordCoreNLP pipeline = new StanfordCoreNLP(props);
		Annotation annotation = pipeline.process(tweet);
		CoreMap sentence = annotation.get(CoreAnnotations.SentencesAnnotation.class).get(0);
		Tree tree = sentence.get(SentimentCoreAnnotations.SentimentAnnotatedTree.class);
		return RNNCoreAnnotations.getPredictedClass(tree);
	}

	// Acquired from ChatGPT
	public boolean keep(String temporalRange) {
    try {
        String[] dates = temporalRange.split("-");
        SimpleDateFormat formatter = new SimpleDateFormat("MMM dd yyyy");

        Date startDate = formatter.parse(dates[0].trim());
        Date endDate = formatter.parse(dates[1].trim());
        Date tweetDate = formatter.parse(this.timestamp);
        return (tweetDate.equals(startDate) || tweetDate.after(startDate)) &&
               (tweetDate.equals(endDate) || tweetDate.before(endDate));
    } catch (ParseException e) {
        e.printStackTrace();
        return false;
    }
}

	
	public String toString() {
		return "{author:" + author + ", sentence:\"" + text + "\", timestamp:\"" + timestamp + "\"}";
	}

	public String getText() {
		return text;
	}

	public String getTimestamp() {
		return timestamp;
	}

	public void setText(String text) {
		this.text = text;
	}

	public String getAuthor() {
		return author;
	}

	public void setAuthor(String author) {
		this.author = author;
	}

	public void setTimestamp(String timestamp) {
		this.timestamp = timestamp;
	}

	public ArrayList<String> splitSentence() {

		ArrayList<String> words = new ArrayList<>();

		String[] pieces = text.split(" ");

		String[] stopwords = { "a", "about", "above", "after", "again", "against", "all", "am", "an", "and", "any",
				"are", "aren't", "as", "at", "be", "because", "been", "before", "being", "below", "between", "both",
				"but", "by", "can't", "cannot", "could", "couldn't", "did", "didn't", "do", "does", "doesn't", "doing",
				"don't", "down", "during", "each", "few", "for", "from", "further", "had", "hadn't", "has", "hasn't",
				"have", "haven't", "having", "he", "he'd", "he'll", "he's", "her", "here", "here's", "hers", "herself",
				"him", "himself", "his", "how", "how's", "i", "i'd", "i'll", "i'm", "i've", "if", "in", "into", "is",
				"isn't", "it", "it's", "its", "itself", "let's", "me", "more", "most", "mustn't", "my", "myself", "no",
				"nor", "not", "of", "off", "on", "once", "only", "or", "other", "ought", "our", "ours ourselves", "out",
				"over", "own", "same", "shan't", "she", "she'd", "she'll", "she's", "should", "shouldn't", "so", "some",
				"such", "than", "that", "that's", "the", "their", "theirs", "them", "themselves", "then", "there",
				"there's", "these", "they", "they'd", "they'll", "they're", "they've", "this", "those", "through", "to",
				"too", "under", "until", "up", "very", "was", "wasn't", "we", "we'd", "we'll", "we're", "we've", "were",
				"weren't", "what", "what's", "when", "when's", "where", "where's", "which", "while", "who", "who's",
				"whom", "why", "why's", "with", "won't", "would", "wouldn't", "you", "you'd", "you'll", "you're",
				"you've", "your", "yours", "yourself", "yourselves" }; // from https://www.ranks.nl/stopwords
		for (int i = 0; i < pieces.length; i++) {
			String piece = pieces[i].toLowerCase();

			boolean isStopWord = false;
			for (int j = 0; j < stopwords.length; j++) {
				if (piece.equals(stopwords[j])) {
					isStopWord = true;
					break;
				}
			}
			if (!piece.isEmpty() && !isStopWord) {
				words.add(piece);
			}

		}
		return words;
	}

	

	public static Sentence convertLine(String line) {
		// first have to initialize an string arraylist, set up a boolean to detect
		// whether or not there are quotes, and create and empy string basket
		ArrayList<String> pieces = new ArrayList<>();
		boolean quotes = false;
		String basket = "";

		// next iterate through the raw data w for loop
		for (int i = 0; i < line.length(); i++) {
			char currentChar = line.charAt(i);
			// write if statement to detect whether or not the currentChar = double quotes,
			// if it does, then we neeed to flip the boolean on, and then off once we hit
			// another double quote

			if (currentChar == '\"') {
				quotes = !quotes;
				// we also need an else if to check if 1. there is a comma and 2. whehther the
				// boolean is set to true (meaning we're in quotes)
			} else if (currentChar == ',' && !quotes) {
				pieces.add(basket);
				basket = "";
				// we also need to account for empty strings, like if therre's a ,, how do we
				// account for that

			}

			else if (line.charAt(i) == ',') {

				if (i + 1 < line.length() && line.charAt(i + 1) == ',') {
					pieces.add("");

				}
			}
			// also need an else statement to deal with the normal characters.
			else {
				basket += currentChar;
			}

		}
		// finally, we need to add what we have in our basket, to the array list pieces
		pieces.add(basket);

		// need to properly format date as well
		String dates = pieces.get(2);
		// We have mm/dd/yyyy
		String[] format = dates.split(" ");
		String[] format1 = format[0].split("/");
		String month = "";

		if (format1[0].equals("1")) {
			month = "January";
		}
		if (format1[0].equals("2")) {
			month = "Febuary";
		}
		if (format1[0].equals("3")) {
			month = "March";
		}
		if (format1[0].equals("4")) {
			month = "April";
		}
		if (format1[0].equals("5")) {
			month = "May";
		}
		if (format1[0].equals("6")) {
			month = "June";
		}
		if (format1[0].equals("7")) {
			month = "July";
		}
		if (format1[0].equals("8")) {
			month = "August";
		}
		if (format1[0].equals("9")) {
			month = "September";
		}
		if (format1[0].equals("10")) {
			month = "October";
		}
		if (format1[0].equals("11")) {
			month = "November";
		}
		if (format1[0].equals("12")) {
			month = "December";
		}
		// 2nd 4th and 7th indexes are date author tweet respectively
		// then return a new Sentence object with these pieces.
		if (pieces.size() >= 8 && format1.length > 0) {
			System.out.println(format1[1]);
			String date = month + " " + format1[1] + " 2020";
			String author = pieces.get(4);
			String text = pieces.get(7);
			return new Sentence(text, author, date);
		} else {
			return null;
		}

	}
}
