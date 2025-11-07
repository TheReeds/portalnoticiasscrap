from fastapi import FastAPI, Depends, HTTPException, Query
from fastapi.middleware.cors import CORSMiddleware
from sqlalchemy.orm import Session
from datetime import datetime, timedelta
from typing import List, Optional
import models
import scrapers

app = FastAPI(title="News Scraper API", version="1.0.0")

# CORS configuration
app.add_middleware(
    CORSMiddleware,
    allow_origins=["*"],  # In production, specify your frontend URL
    allow_credentials=True,
    allow_methods=["*"],
    allow_headers=["*"],
)

# Initialize database
models.init_db()

@app.get("/")
def root():
    return {
        "message": "News Scraper API",
        "endpoints": {
            "scrape": "/scrape",
            "articles": "/articles",
            "sources": "/sources",
            "search": "/articles/search"
        }
    }

@app.post("/scrape")
def scrape_news(
    source: Optional[str] = None,
    limit: int = Query(default=10, ge=1, le=50),
    db: Session = Depends(models.get_db)
):
    """
    Scrape news articles from specified source or all sources

    - **source**: Name of the news source (optional, scrapes all if not specified)
    - **limit**: Number of articles to scrape per source (1-50)
    """
    all_scrapers = scrapers.get_all_scrapers()

    if source:
        # Filter scraper by source name
        selected_scrapers = [s for s in all_scrapers if s.name.lower() == source.lower()]
        if not selected_scrapers:
            raise HTTPException(status_code=404, detail=f"Source '{source}' not found")
    else:
        selected_scrapers = all_scrapers

    scraped_count = 0
    new_articles = []

    for scraper in selected_scrapers:
        print(f"Scraping {scraper.name}...")
        articles = scraper.fetch_articles(limit=limit)

        for article_data in articles:
            # Check if article already exists
            existing = db.query(models.Article).filter(
                models.Article.url == article_data['url']
            ).first()

            if not existing:
                article = models.Article(**article_data)
                db.add(article)
                scraped_count += 1
                new_articles.append(article_data)

        db.commit()

    return {
        "status": "success",
        "scraped": scraped_count,
        "sources": [s.name for s in selected_scrapers],
        "articles": new_articles
    }

@app.get("/articles")
def get_articles(
    skip: int = Query(default=0, ge=0),
    limit: int = Query(default=20, ge=1, le=100),
    source: Optional[str] = None,
    start_date: Optional[str] = None,
    end_date: Optional[str] = None,
    db: Session = Depends(models.get_db)
):
    """
    Get articles with optional filtering

    - **skip**: Number of articles to skip (pagination)
    - **limit**: Maximum number of articles to return (1-100)
    - **source**: Filter by news source
    - **start_date**: Filter articles from this date (YYYY-MM-DD)
    - **end_date**: Filter articles until this date (YYYY-MM-DD)
    """
    query = db.query(models.Article)

    # Filter by source
    if source:
        query = query.filter(models.Article.source.ilike(f"%{source}%"))

    # Filter by date range
    if start_date:
        try:
            start = datetime.fromisoformat(start_date)
            query = query.filter(models.Article.published_date >= start)
        except ValueError:
            raise HTTPException(status_code=400, detail="Invalid start_date format. Use YYYY-MM-DD")

    if end_date:
        try:
            end = datetime.fromisoformat(end_date)
            # Add one day to include the entire end_date
            end = end + timedelta(days=1)
            query = query.filter(models.Article.published_date < end)
        except ValueError:
            raise HTTPException(status_code=400, detail="Invalid end_date format. Use YYYY-MM-DD")

    # Get total count
    total = query.count()

    # Get paginated results
    articles = query.order_by(models.Article.published_date.desc()).offset(skip).limit(limit).all()

    return {
        "total": total,
        "skip": skip,
        "limit": limit,
        "articles": [article.to_dict() for article in articles]
    }

@app.get("/articles/{article_id}")
def get_article(article_id: int, db: Session = Depends(models.get_db)):
    """Get a specific article by ID and increment view count"""
    article = db.query(models.Article).filter(models.Article.id == article_id).first()

    if not article:
        raise HTTPException(status_code=404, detail="Article not found")

    # Increment view count
    article.views += 1
    db.commit()

    return article.to_dict()

@app.get("/articles/search")
def search_articles(
    q: str = Query(..., min_length=1),
    skip: int = Query(default=0, ge=0),
    limit: int = Query(default=20, ge=1, le=100),
    db: Session = Depends(models.get_db)
):
    """
    Search articles by title or content

    - **q**: Search query
    - **skip**: Number of articles to skip (pagination)
    - **limit**: Maximum number of articles to return
    """
    query = db.query(models.Article).filter(
        (models.Article.title.ilike(f"%{q}%")) |
        (models.Article.content.ilike(f"%{q}%"))
    )

    total = query.count()
    articles = query.order_by(models.Article.published_date.desc()).offset(skip).limit(limit).all()

    return {
        "total": total,
        "query": q,
        "articles": [article.to_dict() for article in articles]
    }

@app.get("/sources")
def get_sources(db: Session = Depends(models.get_db)):
    """Get list of available news sources and article counts"""
    all_scrapers = scrapers.get_all_scrapers()

    sources_info = []
    for scraper in all_scrapers:
        count = db.query(models.Article).filter(models.Article.source == scraper.name).count()
        sources_info.append({
            "name": scraper.name,
            "article_count": count
        })

    return {"sources": sources_info}

@app.get("/categories")
def get_categories(db: Session = Depends(models.get_db)):
    """Get list of categories with article counts"""
    from sqlalchemy import func

    categories = db.query(
        models.Article.category,
        func.count(models.Article.id).label('count')
    ).group_by(models.Article.category).all()

    return {
        "categories": [
            {"name": cat, "count": count}
            for cat, count in categories
        ]
    }

@app.get("/articles/popular/top")
def get_popular_articles(
    limit: int = Query(default=10, ge=1, le=50),
    db: Session = Depends(models.get_db)
):
    """Get most viewed articles"""
    articles = db.query(models.Article)\
        .order_by(models.Article.views.desc())\
        .limit(limit)\
        .all()

    return {
        "articles": [article.to_dict() for article in articles]
    }

@app.get("/articles/category/{category}")
def get_articles_by_category(
    category: str,
    skip: int = Query(default=0, ge=0),
    limit: int = Query(default=20, ge=1, le=100),
    db: Session = Depends(models.get_db)
):
    """Get articles by category"""
    query = db.query(models.Article).filter(models.Article.category == category)
    total = query.count()
    articles = query.order_by(models.Article.published_date.desc()).offset(skip).limit(limit).all()

    return {
        "total": total,
        "category": category,
        "articles": [article.to_dict() for article in articles]
    }

@app.delete("/articles/{article_id}")
def delete_article(article_id: int, db: Session = Depends(models.get_db)):
    """Delete a specific article"""
    article = db.query(models.Article).filter(models.Article.id == article_id).first()

    if not article:
        raise HTTPException(status_code=404, detail="Article not found")

    db.delete(article)
    db.commit()

    return {"status": "success", "message": f"Article {article_id} deleted"}

@app.get("/export/articles")
def export_articles(
    format: str = Query(default="json", regex="^(json|csv)$"),
    content_type: Optional[str] = Query(default=None, regex="^(articles|videos|all)$"),
    source: Optional[str] = None,
    db: Session = Depends(models.get_db)
):
    """
    Export articles in JSON or CSV format

    - **format**: Export format (json or csv)
    - **content_type**: Filter by type (articles, videos, or all)
    - **source**: Filter by specific source (optional)
    """
    import csv
    import io
    from fastapi.responses import StreamingResponse

    # Build query
    query = db.query(models.Article)

    if source:
        query = query.filter(models.Article.source == source)

    if content_type == "videos":
        query = query.filter(models.Article.source.contains("YouTube"))
    elif content_type == "articles":
        query = query.filter(~models.Article.source.contains("YouTube"))

    articles = query.order_by(models.Article.published_date.desc()).all()

    if format == "json":
        # Export as JSON
        data = [article.to_dict() for article in articles]
        return {
            "total": len(data),
            "content_type": content_type or "all",
            "source": source or "all",
            "articles": data
        }
    else:
        # Export as CSV
        output = io.StringIO()
        fieldnames = ['id', 'title', 'source', 'author', 'url', 'published_date', 'views', 'category', 'summary']
        writer = csv.DictWriter(output, fieldnames=fieldnames)

        writer.writeheader()
        for article in articles:
            writer.writerow({
                'id': article.id,
                'title': article.title,
                'source': article.source,
                'author': article.author or '',
                'url': article.url,
                'published_date': article.published_date.isoformat() if article.published_date else '',
                'views': article.views,
                'category': article.category,
                'summary': (article.summary or '')[:200]  # Limit summary length for CSV
            })

        output.seek(0)
        return StreamingResponse(
            iter([output.getvalue()]),
            media_type="text/csv",
            headers={"Content-Disposition": f"attachment; filename=news_export_{datetime.now().strftime('%Y%m%d_%H%M%S')}.csv"}
        )

if __name__ == "__main__":
    import uvicorn
    uvicorn.run(app, host="0.0.0.0", port=8000)
