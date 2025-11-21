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

class LaRepublicaScraper(NewsScraper):
    """Scraper for La República (Peru) - No RSS, uses web scraping"""

    def __init__(self):
        super().__init__()
        self.name = "La República"
        self.base_url = "https://larepublica.pe"

    def fetch_articles(self, limit=10):
        """Fetch latest articles from La República homepage"""
        try:
            response = requests.get(f"{self.base_url}/ultimas-noticias", headers=self.headers, timeout=10)
            soup = BeautifulSoup(response.content, 'html.parser')

            # Find article links with date pattern
            all_links = soup.select('a[href*="larepublica.pe"]')
            article_urls = []

            for link in all_links:
                href = link.get('href', '')
                # La República articles have pattern: /YYYY/MM/DD/
                if '/2025/' in href or '/2024/' in href:
                    if not href.startswith('http'):
                        href = self.base_url + href
                    if href not in article_urls:
                        article_urls.append(href)
                        if len(article_urls) >= limit:
                            break

            articles = []
            for url in article_urls[:limit]:
                article_data = {
                    'title': '',
                    'url': url,
                    'source': self.name,
                    'published_date': datetime.utcnow(),
                    'summary': ''
                }

                # Get full content
                full_content = self.scrape_article_content(url)
                article_data.update(full_content)

                if article_data['title']:  # Only add if we got content
                    articles.append(article_data)
                time.sleep(1)

            return articles
        except Exception as e:
            print(f"Error fetching La República articles: {e}")
            return []

    def scrape_article_content(self, url):
        """Scrape full article content using newspaper3k"""
        try:
            article = NewsArticle(url)
            article.download()
            article.parse()

            return {
                'title': article.title,
                'content': article.text,
                'author': ', '.join(article.authors) if article.authors else None,
                'image_url': article.top_image,
                'published_date': article.publish_date or datetime.utcnow(),
                'summary': article.meta_description or article.text[:200] if article.text else ''
            }
        except Exception as e:
            print(f"Error scraping article {url}: {e}")
            return {
                'title': url.split('/')[-1][:50],  # Use URL slug as fallback
                'content': '',
                'author': None,
                'image_url': None
            }


# ==================== CATEGORY-BASED RSS SCRAPERS ====================

class ElComercioCategoryScraper(NewsScraper):
    """Scraper for El Comercio by category"""

    def __init__(self, category="politica"):
        super().__init__()
        self.category = category
        self.name = f"El Comercio - {category.title()}"
        self.rss_url = f"https://elcomercio.pe/{category}/rss"

    def fetch_articles(self, limit=10):
        """Fetch articles from category RSS feed"""
        try:
            feed = feedparser.parse(self.rss_url)
            articles = []

            for entry in feed.entries[:limit]:
                article_data = {
                    'title': entry.get('title', ''),
                    'url': entry.get('link', ''),
                    'source': self.name,
                    'published_date': self.parse_date(entry.get('published', '')),
                    'summary': entry.get('summary', ''),
                    'category': self.category
                }

                full_content = self.scrape_article_content(article_data['url'])
                article_data.update(full_content)

                if article_data['title']:
                    articles.append(article_data)
                time.sleep(0.5)

            return articles
        except Exception as e:
            print(f"Error fetching {self.name} articles: {e}")
            return []

    def scrape_article_content(self, url):
        """Scrape full article content"""
        try:
            article = NewsArticle(url)
            article.download()
            article.parse()

            return {
                'content': article.text,
                'author': ', '.join(article.authors) if article.authors else 'El Comercio',
                'image_url': article.top_image
            }
        except Exception as e:
            print(f"Error scraping content from {url}: {e}")
            return {'content': '', 'author': None, 'image_url': None}


class Peru21CategoryScraper(NewsScraper):
    """Scraper for Perú21 by category"""

    def __init__(self, category="politica"):
        super().__init__()
        self.category = category
        self.name = f"Perú21 - {category.title()}"
        self.rss_url = f"https://peru21.pe/rss/{category}"

    def fetch_articles(self, limit=10):
        """Fetch articles from category RSS feed"""
        try:
            feed = feedparser.parse(self.rss_url)
            articles = []

            for entry in feed.entries[:limit]:
                article_data = {
                    'title': entry.get('title', ''),
                    'url': entry.get('link', ''),
                    'source': self.name,
                    'published_date': self.parse_date(entry.get('published', '')),
                    'summary': entry.get('summary', ''),
                    'category': self.category
                }

                full_content = self.scrape_article_content(article_data['url'])
                article_data.update(full_content)

                if article_data['title']:
                    articles.append(article_data)
                time.sleep(0.5)

            return articles
        except Exception as e:
            print(f"Error fetching {self.name} articles: {e}")
            return []

    def scrape_article_content(self, url):
        """Scrape full article content"""
        try:
            article = NewsArticle(url)
            article.download()
            article.parse()

            return {
                'content': article.text,
                'author': ', '.join(article.authors) if article.authors else 'Perú21',
                'image_url': article.top_image
            }
        except Exception as e:
            print(f"Error scraping content from {url}: {e}")
            return {'content': '', 'author': None, 'image_url': None}


class LaRepublicaCategoryScraper(NewsScraper):
    """Scraper for La República by category"""

    def __init__(self, category="politica"):
        super().__init__()
        self.category = category
        self.name = f"La República - {category.title()}"
        self.rss_url = f"https://larepublica.pe/rss/{category}"

    def fetch_articles(self, limit=10):
        """Fetch articles from category RSS feed"""
        try:
            feed = feedparser.parse(self.rss_url)
            articles = []

            for entry in feed.entries[:limit]:
                article_data = {
                    'title': entry.get('title', ''),
                    'url': entry.get('link', ''),
                    'source': self.name,
                    'published_date': self.parse_date(entry.get('published', '')),
                    'summary': entry.get('summary', ''),
                    'category': self.category
                }

                full_content = self.scrape_article_content(article_data['url'])
                article_data.update(full_content)

                if article_data['title']:
                    articles.append(article_data)
                time.sleep(0.5)

            return articles
        except Exception as e:
            print(f"Error fetching {self.name} articles: {e}")
            return []

    def scrape_article_content(self, url):
        """Scrape full article content"""
        try:
            article = NewsArticle(url)
            article.download()
            article.parse()

            return {
                'content': article.text,
                'author': ', '.join(article.authors) if article.authors else 'La República',
                'image_url': article.top_image
            }
        except Exception as e:
            print(f"Error scraping content from {url}: {e}")
            return {'content': '', 'author': None, 'image_url': None}


class GenericRSSScraper(NewsScraper):
    """Generic Scraper for user-added RSS feeds"""

    def __init__(self, url, name):
        super().__init__()
        self.name = name
        self.rss_url = url

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

                # Try to get full content if possible, but be gentle
                full_content = self.scrape_article_content(article_data['url'])
                article_data.update(full_content)

                if article_data['title']:
                    articles.append(article_data)
                time.sleep(0.5)

            return articles
        except Exception as e:
            print(f"Error fetching {self.name} articles: {e}")
            return []

    def scrape_article_content(self, url):
        """Scrape full article content"""
        try:
            article = NewsArticle(url)
            article.download()
            article.parse()

            return {
                'content': article.text,
                'author': ', '.join(article.authors) if article.authors else self.name,
                'image_url': article.top_image,
                'published_date': article.publish_date or datetime.utcnow()
            }
        except Exception as e:
            print(f"Error scraping content from {url}: {e}")
            return {'content': '', 'author': None, 'image_url': None}


def get_all_scrapers():
    """Return instances of all available scrapers, including user-defined RSS feeds"""
    import models
    
    # Categorías principales para medios peruanos
    categories_peru = ["politica", "economia", "deportes", "mundo"]

    scrapers = [
        # Scrapers principales (feeds generales)
        ElComercioScraper(),
        BBCNewsScraper(),
        GuardianScraper(),
        RPPNoticiasScraper(),
        LaRepublicaScraper(),
        RPPNoticiasYouTubeScraper(),
        DWEspanolYouTubeScraper()
    ]

    # Agregar scrapers por categoría para medios peruanos
    for category in categories_peru:
        scrapers.extend([
            ElComercioCategoryScraper(category),
            Peru21CategoryScraper(category),
            LaRepublicaCategoryScraper(category)
        ])
        
    # Add user-defined RSS feeds
    try:
        db = models.SessionLocal()
        custom_feeds = db.query(models.RSSFeed).all()
        for feed in custom_feeds:
            scrapers.append(GenericRSSScraper(feed.url, feed.name))
        db.close()
    except Exception as e:
        print(f"Error loading custom RSS feeds: {e}")

    return scrapers
