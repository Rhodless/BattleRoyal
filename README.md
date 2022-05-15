## Setup
*Il est nécessaire d'utiliser redis pour la syncronisation des deux serveurs*
Assurez vous d'avoir bien setup le plugin de <a href="https://github.com/Rhodless/BattleRoyal-Sync">synchronisation</a>.
Installez ce plugin sur votre serveur BattleRoyal (que vous avez nommé `battleroyal-event`). Une fois le plugin assurez-vous d'avoir ajouté des points de téléportation avec 
`/br addtp` et d'avoir setup les inventaires (voir les commandes)
## Commandes
![image](https://user-images.githubusercontent.com/58655174/168489840-b4d151c9-f365-40f5-8a7f-2bc9c6e10547.png)
## Configuration
```yml
messages:
  PREFIX: '&c&lSplifight &8&l» &f'
  MORT_DECO: <prefix>&a<player> &fa été éliminé par &cdéconnexion&f.
  MORT: <prefix>&a<player> &fa été éliminé.
  WIN_NOBODY: <prefix>&cPersonne ne gagne la partie...
  WIN_TEAM: <prefix>&fVictoire de l'équipe de &a<winner>
redis:
  host: 127.0.0.1
  port: 6379
  auth: false
  user: user
  password: mon_mot_de_passe
INVENTORY: []
INVENTORY-ARMOR: []
TELEPORT-POINTS: []
```
