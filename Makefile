up:
	docker compose up -d --build

start:
	docker compose up -d

down:
	docker compose down

restart:
	make down && make up

logs:
	docker compose logs -f

ps:
	docker compose ps

clean:
	docker compose down -v --rmi all

rebuild:
	docker compose up -d --build --force-recreate
