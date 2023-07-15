{-# LANGUAGE Rank2Types, ExistentialQuantification #-}

module Terraria
  ()
where

{- 
  Estamos en el mundo de terraria! Tenemos personajes, que tienen un nombre y obviamente llevan ropa. La ropa está conformada por 
  la prenda superior (que puede ser una remera, una camisa o una armadura), la prenda inferior (un jean, un short o unas rodilleras),
  el calzado (unos zapatos, unas zapatillas o unas botas) y el sombrero (un fedora, un agorra o una corona).
  Además, los personajes son re cancheros y usan gafas, ya sea unos lentes de sol para tirar facha o unos anteojos con aumento para ver mejor.

  Adhiriendo a la filosofía de Heráclito: un personaje usa siempre la misma espada, pero va actualizando las medidas según lo necesite.
  Queremos hacer que un personaje pueda decorar su espada con un nuevo pomo.
-}

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

setGraduacion :: Double -> Lentes -> Lentes
setGraduacion graduacion l = l { graduacion = graduacion }

setLentes :: Lentes -> Gafas -> Gafas
setLentes lentes g = g { lentes = lentes }

setGafas :: Gafas -> Personaje -> Personaje
setGafas gafas p = p { gafas = gafas }

setGraduacionGafas :: Double -> Gafas -> Gafas
setGraduacionGafas graduacion g = g { lentes = setGraduacion graduacion . lentes $ g }

cambiarPersonaje :: Double -> Personaje -> Personaje
cambiarPersonaje graduacion p = p { gafas = setGraduacionGafas graduacion . gafas $ p }

graduacionPersonaje :: Personaje -> Double
graduacionPersonaje = graduacion . lentes . gafas

lentesPersonaje :: Personaje -> Lente
lentesPersonaje = lentes . gafas

cambiarMaterialPersonaje :: Material -> Personaje -> Personaje
cambiarMaterialPersonaje nuevoMaterial p = 
  p { gafas = (gafas p) {
      lentes = (lentes $ gafas p) { 
        material = nuevoMaterial }
        }
    }





type Setter a b = (b -> b) -> a -> a

