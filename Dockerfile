FROM php:8.2-apache

RUN apt-get update && apt-get install -y \
    python3 \
    python3-pip \
    && rm -rf /var/lib/apt/lists/* 

RUN docker-php-ext-install mysqli && docker-php-ext-enable mysqli

RUN a2enmod rewrite

COPY ./src /var/www/html

EXPOSE 80

CMD ["apache2-foreground"]

