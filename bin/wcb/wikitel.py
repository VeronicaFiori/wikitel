# coding=utf-8

import json
import re
import wikipediaapi
import wikipedia
import nltk
from flask import Flask, request, jsonify
from flask_restful import Resource, Api
from nltk.corpus import stopwords
from nltk.tokenize import word_tokenize
from sklearn.feature_extraction.text import TfidfVectorizer
from sklearn.metrics.pairwise import cosine_similarity
import it_core_news_sm

nltk.download("stopwords")
nltk.download("punkt")

app = Flask(__name__)
api = Api(app)

wiki = wikipediaapi.Wikipedia("Wikitel", "it")
wikipedia.set_lang("it")
nlp = it_core_news_sm.load()

stop_words = set(stopwords.words("italian"))
stop_words.update([".", ":", ",", ";", "-", "_", "'", "", "(", ")", "/", "!", "?"])

target_categories = {
    "Categoria:Archeologia",
    "Categoria:Architettura",
    "Categoria:Arte",
    "Categoria:Geografia",
    "Categoria:Economia",
    "Categoria:Storia",
    "Categoria:Geografia",
    "Categoria:Scienza",
    "Categoria:Biologia",
    "Categoria:Agricoltura",
    "Categoria:Antropologia",
    "Categoria:Astronomia",
    "Categoria:Botanica",
    "Categoria:Branche_della_fisica",
    "Categoria:Chiese_d%27Italia",
    "Categoria:Chimica",
    "Categoria:Cinema",
    "Categoria:Cultura",
    "Categoria:Elettronica",
    "Categoria:Farmaci",
    "Categoria:Geologia",
    "Categoria:Industria",
    "Categoria:Informatica",
    "Categoria:Ingegneria",
    "Categoria:Letteratura",
    "Categoria:Medicina",
    "Categoria:Musica",
    "Categoria:Natura",
    "Categoria:Opere_d%27arte",
    "Categoria:Politica",
    "Categoria:Psichiatria",
    "Categoria:Psicologia",
    "Categoria:Filosofia",
    "Categoria:Religione",
    "Categoria:Società",
    "Categoria:Sociologia",
    "Categoria:Software",
    "Categoria:Statistica",
    "Categoria:Storia",
    "Categoria:Tecnologia",
}
regexp = re.compile(
    "(Categoria:Senza fonti.*)|(Categoria:Contestualizzare fonti.*)|(Categoria:Informazioni senza fonte.*)|(Categoria:Errori del modulo citazione.*)|(Categoria:Collegamento interprogetto.*)|(Categoria:Categorie.*)|(Categoria:Template.*)|(Categoria:Pagine.*)|(Categoria:Voci.*)|(Categoria:Verificare.*)|(Categoria:P\d+)"
)


def get_category(title):
    cached_categories = set()
    q = list(wiki.page(title).categories)
    q.reverse()
    while q:
        cat = q.pop()
        if cat in target_categories:
            return cat
        cats = list(wiki.page(cat).categories)
        cats.reverse()
        for parent in cats:
            if parent not in cached_categories and not regexp.search(parent):
                cached_categories.add(parent)
                q.append(parent)
    return None


class WikiTEL(Resource):
    def get(self):
        page_title = request.args.get("page")
        try:
            wiki_page = wiki.page(page_title)
            ny = wikipedia.page(page_title)
            if not wiki_page.exists():
                suggestions = wikipedia.suggest(page_title)
                search_results = wikipedia.search(page_title, results=5)
                response_data = {
                    "exists": "false",
                    "suggest": suggestions,
                    "maybe": search_results,
                }
                return response_data
            url = wiki_page.fullurl
            categories = set()
            preconditions = []
            for cat in wiki_page.categories:
                if regexp.search(cat):
                    continue
                if cat in target_categories:
                    categories.add(cat)
                else:
                    c_cat = get_category(cat)
                    if c_cat:
                        categories.add(c_cat)
            doc = nlp(str(wiki_page.text))
            filtrati = [w.text for w in doc if w.text not in stop_words]
            wikiText = str(wiki_page.text)
            docs = [wiki_page.title]
            docs2 = [wikiText]
            for s in wiki_page.links:
                preconditions.append(wiki.page(s).title)
                page_text = str(wiki.page(s).text)
                docs.append(page_text)
                docs2.append(page_text)
            tfidf_vectorizer = TfidfVectorizer()
            tfidf_matrix_train = tfidf_vectorizer.fit_transform(docs)
            tfidf_matrix_train2 = tfidf_vectorizer.fit_transform(docs2)
            rank1 = list(
                cosine_similarity(tfidf_matrix_train[0:1], tfidf_matrix_train)[0]
            )[1:]
            rank2 = list(
                cosine_similarity(tfidf_matrix_train2[0:1], tfidf_matrix_train2)[0]
            )[1:]
            formatted_text = re.sub(r"Note\s*.*", "", wikiText, flags=re.DOTALL)
            response_data = {
                "url": url,
                "categories": list(categories),
                "length": len(filtrati),
                "preconditions": preconditions,
                "rank1": rank1,
                "rank2": rank2,
                "exists": "true",
                "plain_text": formatted_text,
            }
            return response_data
        except Exception as e:
            error_message = f"An error occurred: {str(e)}"
            return {"error": error_message}


api.add_resource(WikiTEL, "/wiki")

if __name__ == "__main__":
    app.run(host="0.0.0.0", port="5015")
