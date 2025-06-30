package com.proyect.ravvisant.core.utils

object UbigeoPeru {
    val departamentos = listOf(
        "Selecciona un departamento",
        "Amazonas",
        "Áncash",
        "Apurímac",
        "Arequipa",
        "Ayacucho",
        "Cajamarca",
        "Callao",
        "Cusco",
        "Huancavelica",
        "Huánuco",
        "Ica",
        "Junín",
        "La Libertad",
        "Lambayeque",
        "Lima",
        "Loreto",
        "Madre de Dios",
        "Moquegua",
        "Pasco",
        "Piura",
        "Puno",
        "San Martín",
        "Tacna",
        "Tumbes",
        "Ucayali"
    )

    val distritosPorDepartamento = mapOf(
        "Amazonas" to listOf(
            "Selecciona un distrito",
            "Chachapoyas", "Asunción", "Balsas", "Cheto", "Chiliquin", "Chuquibamba", "Granada", "Huancas", "La Jalca", "Leimebamba", "Levanto",
            "Magdalena", "Mariscal Castilla", "Molinopampa", "Montevideo", "Olleros", "Quinjalca", "San Francisco de Daguas", "San Isidro de Maino",
            "Soloco", "Sonche", "Jumbilla", "Chisquilla", "Churuja", "Corosha", "Cuispes", "Florida", "Jazan", "Recta", "San Carlos", "Shipasbamba",
            "Valera", "Yambrasbamba", "Nieva", "El Cenepa", "Río Santiago", "Lamud", "Camporredondo", "Cocabamba", "Colcamar", "Conila", "Inguilpata",
            "Longuita", "Lonya Chico", "Luya", "Luya Viejo", "María", "Ocalli", "Ocumal", "Pisuquia", "Providencia", "San Cristóbal", "San Francisco de Yeso",
            "San Jerónimo", "San Juan de Lopecancha", "Santa Catalina", "Santo Tomas", "Tingo", "Trita", "San Nicolás", "Chirimoto", "Cochamal", "Huambo",
            "Limabamba", "Longar", "Mariscal Benavides", "Milpuc", "Omia", "Santa Rosa", "Totora", "Vista Alegre", "Bagua", "Aramango", "Copallin", "El Parco",
            "Imaza", "La Peca"
        ),
        "Áncash" to listOf(
            "Selecciona un distrito",
            "Huaraz", "Cochabamba", "Colcabamba", "Huanchay", "Independencia", "Jangas", "La Libertad", "Olleros", "Pampas", "Pariacoto", "Pira", "Tarica",
            "Aija", "Coris", "Huacllan", "La Merced", "Succha", "Llamellin", "Aczo", "Chaccho", "Chingas", "Mirgas", "San Juan de Rontoy", "Chacas", "Acochaca",
            "Chiquian", "Abelardo Pardo Lezameta", "Antonio Raymondi", "Aquia", "Cajacay", "Canis", "Colquioc", "Huallanca", "Huasta", "Huayllacayan", "La Primavera",
            "Llamellin", "Llipa", "Mangash", "Pacllon", "San Miguel de Corpanqui", "Ticllos", "Carhuaz", "Acopampa", "Amashca", "Anta", "Ataquero", "Marcara",
            "Pariahuanca", "San Miguel de Aco", "Shilla", "Tinco", "Yungar", "San Luis", "San Nicolás", "Yauya", "Casma", "Buena Vista Alta", "Comandante Noel",
            "Yautan", "Corongo", "Aco", "Bambas", "Cusca", "La Pampa", "Yanac", "Yupan", "Huari", "Anra", "Cajay", "Chavin de Huantar", "Huacachi", "Huacchis",
            "Huachis", "Huantar", "Masin", "Paucas", "Ponto", "Rahuapampa", "Rapayan", "San Marcos", "San Pedro de Chana", "Uco", "Huarmey", "Cochapeti",
            "Culebras", "Huayan", "Malvas", "Caraz", "Huallanca", "Huata", "Huaylas", "Mato", "Pamparomas", "Pueblo Libre", "Santa Cruz", "Santo Toribio",
            "Yuracmarca", "Pomabamba", "Huayllan", "Parobamba", "Quinuabamba", "Rondos", "Bolognesi", "Cabana", "Conchucos", "Huacaschuque", "Huandoval",
            "Lacabamba", "Llapo", "Pallasca", "Pampas", "Santa Rosa", "Tauca", "Recuay", "Catac", "Cotaparaco", "Huayllapampa", "Llacllin", "Marca",
            "Pampas Chico", "Pararin", "Tapacocha", "Ticapampa", "Chimbote", "Cáceres del Perú", "Coishco", "Macate", "Moro", "Nepeña", "Samanco", "Santa",
            "Nuevo Chimbote", "Sihuas", "Acobamba", "Alfonso Ugarte", "Cashapampa", "Chingalpo", "Huayllabamba", "Quiches", "Ragash", "San Juan", "Sicsibamba",
            "Yungay", "Cascapara", "Mancos", "Matacoto", "Quillo", "Ranrahirca", "Shupluy", "Yanama"
        ),
        "Apurímac" to listOf(
            "Selecciona un distrito",
            "Abancay", "Chacoche", "Circa", "Curahuasi", "Huanipaca", "Lambrama", "Pichirhua", "San Pedro de Cachora", "Tamburco",
            "Andahuaylas", "Andarapa", "Chiara", "Huancarama", "Huancaray", "Huayana", "Kishuara", "Pacobamba", "Pacucha", "Pampachiri",
            "Pomacocha", "San Antonio de Cachi", "San Jerónimo", "San Miguel de Chaccrampa", "Santa María de Chicmo", "Talavera", "Tumay Huaraca",
            "Turpo", "Kaquiabamba", "José María Arguedas", "Antabamba", "El Oro", "Huaquirca", "Juan Espinoza Medrano", "Oropesa", "Pachaconas",
            "Sabaino", "Chalhuanca", "Capaya", "Caraybamba", "Chapimarca", "Colcabamba", "Cotaruse", "Ihuayllo", "Justo Apu Sahuaraura", "Lucre",
            "Pocohuanca", "San Juan de Chacña", "Sañayca", "Soraya", "Tapairihua", "Tintay", "Toraya", "Yanaca", "Tambobamba", "Cotabambas",
            "Coyllurqui", "Haquira", "Mara", "Challhuahuacho", "Chincheros", "Anco_Huallo", "Cocharcas", "Huaccana", "Ocobamba", "Ongoy",
            "Uranmarca", "Ranracancha", "Rocchacc", "El Porvenir", "Villa Virgen", "Villa Kintiarina", "Santa Rosa"
        ),
        "Arequipa" to listOf(
            "Selecciona un distrito",
            "Arequipa", "Alto Selva Alegre", "Cayma", "Cerro Colorado", "Characato", "Chiguata", "Jacobo Hunter", "La Joya", "Mariano Melgar",
            "Miraflores", "Mollebaya", "Paucarpata", "Pocsi", "Polobaya", "Quequeña", "Sabandía", "Sachaca", "San Juan de Siguas", "San Juan de Tarucani",
            "Santa Isabel de Siguas", "Santa Rita de Siguas", "Socabaya", "Tiabaya", "Uchumayo", "Vitor", "Yanahuara", "Yarabamba", "Yura",
            "Camaná", "José María Quimper", "Mariano Nicolás Valcárcel", "Mariscal Cáceres", "Nicolás de Piérola", "Ocoña", "Quilca", "Samuel Pastor",
            "Caravelí", "Acarí", "Atico", "Atiquipa", "Bella Unión", "Cahuacho", "Chala", "Chaparra", "Huanuhuanu", "Jaqui", "Lomas", "Quicacha",
            "Yauca", "Castilla", "Andagua", "Aplao", "Ayo", "Chachas", "Chilcaymarca", "Choco", "Huancarqui", "Machaguay", "Orcopampa", "Pampacolca",
            "Tipán", "Uñón", "Uraca", "Viraco", "Condesuyos", "Chuquibamba", "La Unión", "Alca", "Charcana", "Cotahuasi", "Huaynacotas", "Pampamarca",
            "Puyca", "Quechualla", "Sayla", "Tauria", "Tomepampa", "Toro", "Islay", "Cocachacra", "Dean Valdivia", "Islay", "Mejia", "Mollendo", "Punta de Bombón",
            "La Unión", "Cotahuasi", "Pampamarca", "Toro"
        ),
        "Ayacucho" to listOf(
            "Selecciona un distrito",
            "Ayacucho", "Acocro", "Acos Vinchos", "Carmen Alto", "Chiara", "Ocros", "Pacaycasa", "Quinua", "San José de Ticllas",
            "San Juan Bautista", "Santiago de Pischa", "Socos", "Tambillo", "Vinchos", "Jesús Nazareno", "Cangallo", "Chuschi", "Los Morochucos",
            "María Parado de Bellido", "Paras", "Totos", "San Miguel", "Anco", "Chuschi", "Huanca-Huanca", "Huanta", "Huamanguilla", "Iguaín",
            "Luricocha", "Santillana", "Sivia", "Llochegua", "San Francisco", "Santa Rosa", "Putis"
        ),
        "Cajamarca" to listOf(
            "Selecciona un distrito",
            "Cajamarca", "Asunción", "Chetilla", "Cospán", "Encañada", "Jesús", "Llacanora", "Los Baños del Inca", "Magdalena", "Matara", "Namora",
            "San Juan", "Cajabamba", "Cachachi", "Condebamba", "Sitacocha", "Celendín", "Chumuch", "Cortegana", "Huasmin", "Jorge Chávez", "José Gálvez",
            "La Libertad de Pallán", "Miguel Iglesias", "Oxamarca", "Sorochuco", "Sucre", "Utco", "La Libertad", "San Miguel", "Bolívar", "Calquis",
            "Catilluc", "El Prado", "La Florida", "Llapa", "Nanchoc", "Niepos", "San Gregorio", "San Silvestre de Cochan", "Tongod", "Union Agua Blanca"
        ),
        "Callao" to listOf(
            "Selecciona un distrito",
            "Callao", "Bellavista", "Carmen de la Legua Reynoso", "La Perla", "La Punta", "Ventanilla", "Mi Perú"
        ),
        "Cusco" to listOf(
            "Selecciona un distrito",
            "Cusco", "Ccorca", "Poroy", "San Jerónimo", "San Sebastián", "Santiago", "Saylla", "Wanchaq", "Acomayo", "Acopia", "Acos", "Mosoc Llacta",
            "Pomacanchi", "Rondocan", "Sangarará", "Anta", "Ancahuasi", "Cachimayo", "Chinchaypujio", "Huarocondo", "Limatambo", "Mollepata",
            "Pucyura", "Zurite", "Calca", "Coya", "Lamay", "Lares", "Pisac", "San Salvador", "Taray", "Yanatile", "Canas", "Checca", "Kunturkanki",
            "Langui", "Layo", "Pampamarca", "Quehue", "Tupac Amaru", "Canchis", "Checacupe", "Combapata", "Marangani", "Pitumarca", "San Pablo",
            "San Pedro", "Tinta"
        ),
        "Huancavelica" to listOf(
            "Selecciona un distrito",
            "Huancavelica", "Acobambilla", "Acoria", "Conayca", "Cuenca", "Huachocolpa", "Huayllahuara", "Izcuchaca", "Laria", "Manta", "Mariscal Cáceres",
            "Moya", "Nuevo Occoro", "Palca", "Pilchaca", "Vilca", "Yauli", "Ascensión", "Huando"
        ),
        "Huánuco" to listOf(
            "Selecciona un distrito",
            "Huánuco", "Amarilis", "Chinchao", "Churubamba", "Margos", "Quisqui", "San Francisco de Cayrán", "San Pedro de Chaulán", "Santa María del Valle",
            "Yarowilca", "Yacus", "San Pablo de Pillao"
        ),
        "Ica" to listOf(
            "Selecciona un distrito",
            "Ica", "La Tinguiña", "Los Aquijes", "Ocucaje", "Pachacutec", "Parcona", "Pueblo Nuevo", "Salas", "San José de los Molinos", "San Juan Bautista",
            "Santiago", "Subtanjalla", "Tate", "Yauca del Rosario"
        ),
        "Junín" to listOf(
            "Selecciona un distrito",
            "Huancayo", "Carhuacallanga", "Chacapampa", "Chicche", "Chilca", "Chongos Alto", "Chupuro", "Colca", "Cullhuas", "El Tambo", "Huacrapuquio",
            "Hualhuas", "Huancan", "Huasicancha", "Huayucachi", "Ingenio", "Pariahuanca", "Pilcomayo", "Pucara", "Quichuay", "Quilcas", "San Agustín",
            "San Jerónimo de Tunán", "Saño", "Sapallanga", "Sicaya", "Santo Domingo de Acobamba", "Viques"
        ),
        "La Libertad" to listOf(
            "Selecciona un distrito",
            "Trujillo", "El Porvenir", "Florencia de Mora", "Huanchaco", "La Esperanza", "Laredo", "Moche", "Poroto", "Salaverry", "Simbal", "Victor Larco Herrera"
        ),
        "Lambayeque" to listOf(
            "Selecciona un distrito",
            "Chiclayo", "Chongoyape", "Eten", "Eten Puerto", "José Leonardo Ortiz", "La Victoria", "Lagunas", "Monsefú", "Nueva Arica", "Oyotún", "Picsi",
            "Pimentel", "Reque", "Santa Rosa", "Saña", "Cayaltí", "Patapo", "Pomalca", "Pucalá", "Tuman"
        ),
        "Lima" to listOf(
            "Selecciona un distrito",
            "Lima", "Ancón", "Ate", "Barranco", "Breña", "Carabayllo", "Chaclacayo", "Chorrillos", "Cieneguilla", "Comas", "El Agustino", "Independencia",
            "Jesús María", "La Molina", "La Victoria", "Lince", "Los Olivos", "Lurigancho", "Lurín", "Magdalena del Mar", "Miraflores", "Pachacamac",
            "Pucusana", "Pueblo Libre", "Puente Piedra", "Punta Hermosa", "Punta Negra", "Rímac", "San Bartolo", "San Borja", "San Isidro", "San Juan de Lurigancho",
            "San Juan de Miraflores", "San Luis", "San Martín de Porres", "San Miguel", "Santa Anita", "Santa María del Mar", "Santa Rosa", "Santiago de Surco",
            "Surquillo", "Villa El Salvador", "Villa María del Triunfo"
        ),
        "Loreto" to listOf(
            "Selecciona un distrito",
            "Iquitos", "Alto Nanay", "Fernando Lores", "Indiana", "Las Amazonas", "Mazan", "Napo", "Punchana", "Torres Causana", "Belén", "San Juan Bautista"
        ),
        "Madre de Dios" to listOf(
            "Selecciona un distrito",
            "Tambopata", "Inambari", "Las Piedras", "Laberinto", "Manu", "Fitzcarrald", "Madre de Dios", "Huepetuhe", "Iberia", "Iñapari", "Tahuamanu"
        ),
        "Moquegua" to listOf(
            "Selecciona un distrito",
            "Moquegua", "Carumas", "Cuchumbaya", "Samegua", "San Cristóbal", "Torata", "Ilo", "El Algarrobal", "Pacocha", "Mariscal Nieto"
        ),
        "Pasco" to listOf(
            "Selecciona un distrito",
            "Chaupimarca", "Huachón", "Huariaca", "Huayllay", "Ninacaca", "Pallanchacra", "Paucartambo", "San Francisco de Asís de Yarusyacan", "Simon Bolívar",
            "Ticlacayán", "Tinyahuarco", "Vicco", "Yanacancha"
        ),
        "Piura" to listOf(
            "Selecciona un distrito",
            "Piura", "Castilla", "Catacaos", "Cura Mori", "El Tallán", "La Arena", "La Unión", "Las Lomas", "Tambo Grande", "Veintiséis de Octubre"
        ),
        "Puno" to listOf(
            "Selecciona un distrito",
            "Puno", "Acora", "Amantani", "Atuncolla", "Capachica", "Chucuito", "Coata", "Huata", "Mañazo", "Paucarcolla", "Pichacani", "Plateria", "San Antonio",
            "Tiquillaca", "Vilque"
        ),
        "San Martín" to listOf(
            "Selecciona un distrito",
            "Moyobamba", "Calzada", "Habana", "Jepelacio", "Soritor", "Yantalo", "Tarapoto", "Alberto Leveau", "Cacatachi", "Chazuta", "Chipurana",
            "El Porvenir", "Huimbayoc", "Juan Guerra", "La Banda de Shilcayo", "Morales", "Papaplaya", "San Antonio", "Sauce", "Shapaja"
        ),
        "Tacna" to listOf(
            "Selecciona un distrito",
            "Tacna", "Alto de la Alianza", "Calana", "Ciudad Nueva", "Inclán", "Pachía", "Palca", "Pocollay", "Sama", "Coronel Gregorio Albarracín Lanchipa"
        ),
        "Tumbes" to listOf(
            "Selecciona un distrito",
            "Tumbes", "Corrales", "La Cruz", "Pampas de Hospital", "San Jacinto", "San Juan de la Virgen", "Zorritos", "Casitas", "Canoas de Punta Sal"
        ),
        "Ucayali" to listOf(
            "Selecciona un distrito",
            "Pucallpa", "Campoverde", "Iparia", "Masisea", "Yarinacocha", "Nueva Requena", "Manantay", "Padre Abad", "Irazola", "Curimana", "Neshuya",
            "Alexander Von Humboldt", "Raimondi", "Sepahua", "Tahuania", "Yurua", "Purús"
        )
    )
}