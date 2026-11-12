ALTER TABLE exercices
    ADD COLUMN objectif varchar(255) NULL,
  ADD COLUMN instructions text NULL,
  ADD COLUMN muscles varchar(150) NULL,
  ADD COLUMN class_keywords varchar(255) NULL;

-- default keywords by category (which classes relate to this exercise)
UPDATE exercices SET class_keywords = CASE categorie
                                          WHEN 'CARDIO'      THEN 'cardio,hiit,body attack,step'
                                          WHEN 'HIIT'        THEN 'hiit,cardio,body attack,cross'
                                          WHEN 'MUSCULATION' THEN 'musculation,body pump,renfo,cross'
                                          WHEN 'GAINAGE'     THEN 'gainage,core,pilates,abdo'
                                          WHEN 'STRETCHING'  THEN 'stretching,yoga,pilates'
                                          ELSE NULL END;

-- goal, muscles and steps for the exercises in your screenshot (one step per line)
UPDATE exercices SET
                     objectif = 'Renforcer les pectoraux, les épaules et les triceps avec le seul poids du corps.',
                     muscles = 'Pectoraux, Épaules, Triceps, Abdominaux',
                     instructions = CONCAT_WS(CHAR(10),
                             'Placez les mains un peu plus larges que les épaules, bras tendus.',
                             'Alignez la tête, le dos et les jambes : le corps forme une ligne droite.',
                             'Descendez en fléchissant les coudes jusqu''à frôler le sol, sans cambrer le dos.',
                             'Poussez pour remonter en expirant.',
                             'Réalisez 3 séries de 8 à 12 répétitions, avec 60 secondes de repos.')
WHERE LOWER(nom) LIKE '%pompes%';

UPDATE exercices SET
                     objectif = 'Développer la force des jambes et des fessiers, et améliorer la stabilité du bassin.',
                     muscles = 'Quadriceps, Fessiers, Ischio-jambiers, Abdominaux',
                     instructions = CONCAT_WS(CHAR(10),
                             'Placez les pieds à la largeur des épaules, pointes légèrement ouvertes.',
                             'Gardez le dos droit et le regard devant vous.',
                             'Descendez en poussant les fesses vers l''arrière, jusqu''à ce que les cuisses soient parallèles au sol.',
                             'Les genoux restent dans l''axe des pieds.',
                             'Remontez en poussant dans les talons. Faites 3 séries de 10 à 15 répétitions.')
WHERE LOWER(nom) LIKE '%squat%';

UPDATE exercices SET
                     objectif = 'Brûler un maximum de calories et améliorer le cardio grâce à un mouvement complet.',
                     muscles = 'Corps entier, Cardio',
                     instructions = CONCAT_WS(CHAR(10),
                             'Debout, descendez en squat et posez les mains au sol.',
                             'Lancez les jambes en arrière pour arriver en position de pompe.',
                             'Ramenez les pieds près des mains.',
                             'Sautez en tendant les bras vers le haut.',
                             'Enchaînez pendant 30 secondes, repos 30 secondes, 4 à 6 tours.')
WHERE LOWER(nom) LIKE '%burpee%';

UPDATE exercices SET
                     objectif = 'Améliorer l''endurance et la santé cardiovasculaire, et brûler des graisses.',
                     muscles = 'Jambes, Cardio',
                     instructions = CONCAT_WS(CHAR(10),
                             'Échauffez-vous 5 minutes en marchant à allure modérée.',
                             'Passez à une allure de course confortable : vous devez pouvoir parler.',
                             'Gardez le buste droit, les épaules relâchées, les bras fléchis à 90 degrés.',
                             'Terminez par 5 minutes de marche pour récupérer.')
WHERE LOWER(nom) LIKE '%tapis%' OR LOWER(nom) LIKE '%course%';

UPDATE exercices SET
                     objectif = 'Développer le dos et les biceps en soulevant le poids du corps.',
                     muscles = 'Grand dorsal, Biceps, Épaules',
                     instructions = CONCAT_WS(CHAR(10),
                             'Saisissez la barre, mains un peu plus larges que les épaules, paumes vers l''avant.',
                             'Suspendez-vous bras tendus, épaules engagées vers le bas.',
                             'Tirez les coudes vers le sol jusqu''à passer le menton au-dessus de la barre.',
                             'Redescendez lentement, en contrôlant le mouvement.',
                             'Faites 3 séries aussi longues que possible.')
WHERE LOWER(nom) LIKE '%traction%';

UPDATE exercices SET
                     objectif = 'Renforcer les muscles profonds de l''abdomen et stabiliser le tronc.',
                     muscles = 'Abdominaux, Dos, Épaules',
                     instructions = CONCAT_WS(CHAR(10),
                             'Placez les coudes sous les épaules, avant-bras au sol.',
                             'Tendez les jambes, appuyez-vous sur la pointe des pieds.',
                             'Contractez le ventre et les fessiers : le corps reste aligné, sans creuser le dos.',
                             'Respirez calmement et tenez 20 à 45 secondes.',
                             'Répétez 3 fois avec 30 secondes de repos.')
WHERE LOWER(nom) LIKE '%gainage%' OR LOWER(nom) LIKE '%planche%';