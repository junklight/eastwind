
local Eastwind = {}
local ControlSpec = require 'controlspec'
local Formatters = require 'formatters'

-- helper function to round and format parameter value text:
function round_form(param,quant,form)
  return(util.round(param,quant)..form)
end

function Eastwind.add_params()
  params:add_separator("Eastwind")
  
  params:add_group("sound")
-- attack
  params:add_number(
          'attack'
          'attack',
          0.05,
          10,
          0.3,
          function(param) return (round_form(param:get(),0.01," s")) end
        )
  params:set_action('attack',function(x) engine.attack(x) end )
-- release 
  params:add_number(
          'release'
          'release',
          0.05,
          10,
          0.3,
          function(param) return (round_form(param:get(),0.01," s")) end
        )
  params:set_action('release',function(x) engine.release(x) end )        
-- smix 
  params:add_number(
          'smix'
          'sound',
          0,
          1,
          0.3,
          function(param) return (round_form(param:get(),0.01,"")) end
        )
  params:set_action('smix',function(x) engine.smix(x) end )                
-- amp 
  params:add_number(
          'amp'
          'amp',
          0,
          0.9,
          0.4,
          function(param) return (round_form(param:get()*100,1,"%")) end
        )
  params:set_action('amp',function(x) engine.amp(x) end )                
  params:add_group("resontator")
-- resonantor 
-- decay 
  params:add_number(
          'decay'
          'decay',
          0.05,
          20,
          3,
          function(param) return (round_form(param:get(),0.01," s")) end
        )
  params:set_action('decay',function(x) engine.decay(x) end )                        
  -- amp 
  params:add_number(
          'decayamp'
          'decayamp',
          0,
          0.9,
          0.4,
          function(param) return (round_form(param:get()*100,1,"%")) end
        )
  params:set_action('decayamp',function(x) engine.decayamp(x) end )                        

-- no looper controls for the moment 
  params:bang()
end


 -- we return these engine-specific Lua functions back to the host script:
return Eastwind