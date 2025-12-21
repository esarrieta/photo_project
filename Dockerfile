FROM php:8.2-apache

RUN docker-php-ext-install mysqli && docker-php-ext-enable mysqli

RUN a2enmod rewrite

COPY ./src /var/www/html

EXPOSE 80

CMD ["apache2-foreground"]

