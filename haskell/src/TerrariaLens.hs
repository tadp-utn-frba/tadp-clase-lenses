{-# LANGUAGE TemplateHaskell, Rank2Types #-}

module TerrariaLens where

import Control.Lens

-- La biblioteca puede autogenerar los lenses para nuestros datos si les agregamos un _ adelante de su nombre

data Personaje = Personaje {
  _nombre :: String,
  _gafas :: Gafas,
  _ropa :: Ropa
}

data Ropa = Ropa {
  _superior :: String,
  _inferior :: String,
  _calzado :: String,
  _sombrero :: String
}

data Gafas = Gafas {
  _armazon :: Armazon,
  _lentes :: Lentes,
  _decoracion :: Decoracion
}

data Lentes = Lentes {
  _material :: Material,
  _graduacion :: Double
}

data Armazon = Hierro | Oro | Plata
data Material = Cristal | Vidrio | Polimero
data Decoracion = Rubi | Zafiro | Esmeralda

robert = Personaje
  { _nombre = "Roberto"
  , _ropa   = Ropa
    { _superior = "Remera roja"
    , _inferior = "Jean azul"
    , _calzado  = "Ojotas cancheras"
    , _sombrero = "Gorro de navidad"
    }
  , _gafas  = Gafas { _armazon = Hierro, _decoracion = Esmeralda, _lentes = Lentes { _material = Polimero, _graduacion = 0.25 } }
  }

-- Estas funciones generan los lenses! Pero si no lo ponemos, podemos definirlos nosotros a mano

makeLenses ''Gafas
makeLenses ''Ropa
makeLenses ''Personaje

graduacion :: Lens' Lentes Double
graduacion = lens _graduacion (\lentes graduacion -> lentes { _graduacion = graduacion })

material :: Lens' Lentes Material
material = lens _material (\lentes material -> lentes { _material = material })

cambiarGraduacion :: Double -> Personaje -> Personaje
cambiarGraduacion = over (gafas . lentes . graduacion) . const

graduacionPersonaje :: Personaje -> Double
graduacionPersonaje = view (gafas . lentes . graduacion)
