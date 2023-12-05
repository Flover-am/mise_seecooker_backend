docker build -f Dockerfile_deploy -t seecooker:0.0.1 .;
docker stop seecooker_deploy;
docker rm seecooker_deploy;
docker run --name seecooker_deploy \
    -itd -p 8080:8080 \
    -e AccesskeyID=LTAI5tN3o8739ftwMyspbAgc \
    -e AccesskeySecret=PqJH63OBowxfXJeu5v0Tc21lX6EVvX \
    seecooker:0.0.1