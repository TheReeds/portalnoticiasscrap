import feedparser
import requests
from bs4 import BeautifulSoup
from newspaper import Article as NewsArticle
from datetime import datetime
from dateutil import parser as date_parser
import time

class NewsScraper:
    """Base class for news scraping"""

    def __init__(self):
        self.headers = {
            'User-Agent': 'Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/91.0.4472.124 Safari/537.36'
        }

    def parse_date(self, date_string):
        """Parse various date formats"""
        try:
            if isinstance(date_string, datetime):
                return date_string
            return date_parser.parse(date_string)
        except:
            return datetime.utcnow()

class ElComercioScraper(NewsScraper):
    """Scraper for El Comercio (Peru)"""

    def __init__(self):
        super().__init__()
        self.name = "El Comercio"
        # Using the main RSS feed which has content
        self.rss_url = "https://elcomercio.pe/arc/outboundfeeds/rss/?outputType=xml"

    def fetch_articles(self, limit=10):
        """Fetch articles from RSS feed"""
        try:
            feed = feedparser.parse(self.rss_url)
            articles = []

            for entry in feed.entries[:limit]:
                article_data = {
                    'title': entry.get('title', ''),
                    'url': entry.get('link', ''),
                    'source': self.name,
                    'published_date': self.parse_date(entry.get('published', '')),
                    'summary': entry.get('summary', '')
                }

                # Get full content
                full_content = self.scrape_article_content(article_data['url'])
                article_data.update(full_content)

                articles.append(article_data)
                time.sleep(1)  # Be respectful with rate limiting

            return articles
        except Exception as e:
            print(f"Error fetching El Comercio articles: {e}")
            return []

    def scrape_article_content(self, url):
        """Scrape full article content using newspaper3k"""
        try:
            article = NewsArticle(url)
            article.download()
            article.parse()

            return {
                'content': article.text,
                'author': ', '.join(article.authors) if article.authors else None,
                'image_url': article.top_image,
                'published_date': article.publish_date or datetime.utcnow()
            }
        except Exception as e:
            print(f"Error scraping article {url}: {e}")
            return {
                'content': '',
                'author': None,
                'image_url': None
            }

class BBCNewsScraper(NewsScraper):
    """Scraper for BBC News"""

    def __init__(self):
        super().__init__()
        self.name = "BBC News"
        self.rss_url = "http://feeds.bbci.co.uk/news/rss.xml"

    def fetch_articles(self, limit=10):
        """Fetch articles from RSS feed"""
        try:
            feed = feedparser.parse(self.rss_url)
            articles = []

            for entry in feed.entries[:limit]:
                article_data = {
                    'title': entry.get('title', ''),
                    'url': entry.get('link', ''),
                    'source': self.name,
                    'published_date': self.parse_date(entry.get('published', '')),
                    'summary': entry.get('summary', '')
                }

                # Get full content
                full_content = self.scrape_article_content(article_data['url'])
                article_data.update(full_content)

                articles.append(article_data)
                time.sleep(1)

            return articles
        except Exception as e:
            print(f"Error fetching BBC News articles: {e}")
            return []

    def scrape_article_content(self, url):
        """Scrape full article content"""
        try:
            article = NewsArticle(url)
            article.download()
            article.parse()

            return {
                'content': article.text,
                'author': ', '.join(article.authors) if article.authors else 'BBC News',
                'image_url': article.top_image,
                'published_date': article.publish_date or datetime.utcnow()
            }
        except Exception as e:
            print(f"Error scraping BBC article {url}: {e}")
            return {
                'content': '',
                'author': 'BBC News',
                'image_url': None
            }

class GuardianScraper(NewsScraper):
    """Scraper for The Guardian"""

    def __init__(self):
        super().__init__()
        self.name = "The Guardian"
        self.rss_url = "https://www.theguardian.com/world/rss"

    def fetch_articles(self, limit=10):
        """Fetch articles from RSS feed"""
        try:
            feed = feedparser.parse(self.rss_url)
            articles = []

            for entry in feed.entries[:limit]:
                article_data = {
                    'title': entry.get('title', ''),
                    'url': entry.get('link', ''),
                    'source': self.name,
                    'published_date': self.parse_date(entry.get('published', '')),
                    'summary': entry.get('summary', '')
                }

                # Get full content
                full_content = self.scrape_article_content(article_data['url'])
                article_data.update(full_content)

                articles.append(article_data)
                time.sleep(1)

            return articles
        except Exception as e:
            print(f"Error fetching Guardian articles: {e}")
            return []

    def scrape_article_content(self, url):
        """Scrape full article content"""
        try:
            article = NewsArticle(url)
            article.download()
            article.parse()

            return {
                'content': article.text,
                'author': ', '.join(article.authors) if article.authors else 'The Guardian',
                'image_url': article.top_image,
                'published_date': article.publish_date or datetime.utcnow()
            }
        except Exception as e:
            print(f"Error scraping Guardian article {url}: {e}")
            return {
                'content': '',
                'author': 'The Guardian',
                'image_url': None
            }

class RPPNoticiasScraper(NewsScraper):
    """Scraper for RPP Noticias (Peru)"""

    def __init__(self):
        super().__init__()
        self.name = "RPP Noticias"
        self.rss_url = "https://rpp.pe/rss"

    def fetch_articles(self, limit=10):
        """Fetch articles from RSS feed"""
        try:
            feed = feedparser.parse(self.rss_url)
            articles = []

            for entry in feed.entries[:limit]:
                article_data = {
                    'title': entry.get('title', ''),
                    'url': entry.get('link', ''),
                    'source': self.name,
                    'published_date': self.parse_date(entry.get('published', '')),
                    'summary': entry.get('summary', '')
                }

                # Get full content
                full_content = self.scrape_article_content(article_data['url'])
                article_data.update(full_content)

                articles.append(article_data)
                time.sleep(1)  # Be respectful with rate limiting

            return articles
        except Exception as e:
            print(f"Error fetching RPP Noticias articles: {e}")
            return []

    def scrape_article_content(self, url):
        """Scrape full article content using newspaper3k"""
        try:
            article = NewsArticle(url)
            article.download()
            article.parse()

            return {
                'content': article.text,
                'author': ', '.join(article.authors) if article.authors else 'RPP Noticias',
                'image_url': article.top_image,
                'published_date': article.publish_date or datetime.utcnow()
            }
        except Exception as e:
            print(f"Error scraping article {url}: {e}")
            return {
                'content': '',
                'author': 'RPP Noticias',
                'image_url': None
            }

class RPPNoticiasYouTubeScraper(NewsScraper):
    """Scraper for RPP Noticias YouTube Channel"""

    def __init__(self):
        super().__init__()
        self.name = "RPP Noticias YouTube"
        # RPP Noticias YouTube channel RSS feed
        self.channel_id = "UC5j8-2FT0ZMMBkmK72R4aeA"
        self.rss_url = f"https://www.youtube.com/feeds/videos.xml?channel_id={self.channel_id}"

    def fetch_articles(self, limit=10):
        """Fetch videos from YouTube RSS feed"""
        try:
            feed = feedparser.parse(self.rss_url)
            articles = []

            for entry in feed.entries[:limit]:
                # Extract video data from YouTube RSS
                video_id = entry.get('yt_videoid', '')
                video_url = f"https://www.youtube.com/watch?v={video_id}"

                # Get thumbnail - YouTube provides standard thumbnail URLs
                thumbnail_url = f"https://i.ytimg.com/vi/{video_id}/hqdefault.jpg"

                article_data = {
                    'title': entry.get('title', ''),
                    'url': video_url,
                    'source': self.name,
                    'published_date': self.parse_date(entry.get('published', '')),
                    'summary': entry.get('summary', '')[:500] if entry.get('summary') else '',
                    'content': entry.get('summary', ''),
                    'author': entry.get('author', 'RPP Noticias'),
                    'image_url': thumbnail_url
                }

                articles.append(article_data)
                time.sleep(0.5)  # Light rate limiting for RSS

            return articles
        except Exception as e:
            print(f"Error fetching RPP YouTube videos: {e}")
            return []

class DWEspanolYouTubeScraper(NewsScraper):
    """Scraper for DW Español YouTube Channel"""

    def __init__(self):
        super().__init__()
        self.name = "DW Español YouTube"
        # DW Español YouTube channel RSS feed
        self.channel_id = "UCT4Jg8h03dD0iN3Pb5L0PMA"
        self.rss_url = f"https://www.youtube.com/feeds/videos.xml?channel_id={self.channel_id}"

    def fetch_articles(self, limit=10):
        """Fetch videos from YouTube RSS feed"""
        try:
            feed = feedparser.parse(self.rss_url)
            articles = []

            for entry in feed.entries[:limit]:
                # Extract video data from YouTube RSS
                video_id = entry.get('yt_videoid', '')
                video_url = f"https://www.youtube.com/watch?v={video_id}"

                # Get thumbnail
                thumbnail_url = f"https://i.ytimg.com/vi/{video_id}/hqdefault.jpg"

                article_data = {
                    'title': entry.get('title', ''),
                    'url': video_url,
                    'source': self.name,
                    'published_date': self.parse_date(entry.get('published', '')),
                    'summary': entry.get('summary', '')[:500] if entry.get('summary') else '',
                    'content': entry.get('summary', ''),
                    'author': entry.get('author', 'DW Español'),
                    'image_url': thumbnail_url
                }

                articles.append(article_data)
                time.sleep(0.5)  # Light rate limiting for RSS

            return articles
        except Exception as e:
            print(f"Error fetching DW Español YouTube videos: {e}")
            return []

def get_all_scrapers():
    """Return instances of all available scrapers"""
    return [
        ElComercioScraper(),
        BBCNewsScraper(),
        GuardianScraper(),
        RPPNoticiasScraper(),
        RPPNoticiasYouTubeScraper(),
        DWEspanolYouTubeScraper()
    ]
