from sqlalchemy import Column, Integer, String, Text, DateTime, create_engine
from sqlalchemy.ext.declarative import declarative_base
from sqlalchemy.orm import sessionmaker
from datetime import datetime

Base = declarative_base()

class Article(Base):
    __tablename__ = "articles"

    id = Column(Integer, primary_key=True, index=True)
    title = Column(String(500), nullable=False)
    url = Column(String(1000), unique=True, nullable=False)
    source = Column(String(100), nullable=False)
    author = Column(String(200))
    published_date = Column(DateTime)
    content = Column(Text)
    summary = Column(Text)
    image_url = Column(String(1000))
    category = Column(String(100), default="general")  # política, deportes, economía, etc
    views = Column(Integer, default=0)  # Contador de vistas
    scraped_at = Column(DateTime, default=datetime.utcnow)

    def to_dict(self):
        return {
            "id": self.id,
            "title": self.title,
            "url": self.url,
            "source": self.source,
            "author": self.author,
            "published_date": self.published_date.isoformat() if self.published_date else None,
            "content": self.content,
            "summary": self.summary,
            "image_url": self.image_url,
            "category": self.category,
            "views": self.views,
            "scraped_at": self.scraped_at.isoformat() if self.scraped_at else None
        }

# Database setup
SQLALCHEMY_DATABASE_URL = "sqlite:///./news.db"
engine = create_engine(SQLALCHEMY_DATABASE_URL, connect_args={"check_same_thread": False})
SessionLocal = sessionmaker(autocommit=False, autoflush=False, bind=engine)

def init_db():
    Base.metadata.create_all(bind=engine)

def get_db():
    db = SessionLocal()
    try:
        yield db
    finally:
        db.close()
