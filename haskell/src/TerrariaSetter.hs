{-# LANGUAGE RankNTypes #-}

module TerrariaSetter
  ()
where

data Personaje = Personaje {
  nombre :: String,
  gafas :: Gafas,
  ropa :: Ropa
}

data Ropa = Ropa {
  superior :: String,
  inferior :: String,
  calzado :: String,
  sombrero :: String
}

data Gafas = Gafas {
  armazon :: Armazon,
  lentes :: Lentes,
  decoracion :: Decoracion
}

data Lentes = Lente {
  material :: Material,
  graduacion :: Double
}

data Armazon = Hierro | Oro | Plata
data Material = Cristal | Vidrio | Polimero
data Decoracion = Rubi | Zafiro | Esmeralda

type Setter a b = (b -> b) -> (a -> a)

setter :: (a -> b) -> (a -> b -> a) -> (b -> b) -> (a -> a)
setter accessor modify update a = modify a . update . accessor $ a

--setGraduacion :: Setter Lentes Double
setGraduacion = setter graduacion (\lentes graduacion -> lentes { graduacion = graduacion })

setMaterial :: Material -> Lentes -> Lentes
setMaterial = setter material (\lentes material -> lentes { material = material })

--setLentes :: Setter Gafas Lentes
setLentes = setter lentes (\gafas lentes -> gafas { lentes = lentes })

--setGafas :: Setter Personaje Gafas
setGafas = setter gafas (\personaje gafas -> personaje { gafas = gafas })

graduacionPersonaje :: Setter Personaje Double
graduacionPersonaje = setGafas . setLentes . setGraduacion

cambiarMaterialPersonaje :: Setter Personaje Material
cambiarMaterialPersonaje = setGafas . setLentes . setMaterial

cambiarGraduacion :: Double -> Personaje -> Personaje
cambiarGraduacion = graduacionPersonaje . const
