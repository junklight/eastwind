-- GridControl
-- @module lib.GridControl

local GridControl = {}
GridControl.__index = GridControl

--- constructor
-- @tparam string name
-- @treturn BeatClock
function GridControl.new(y,valfn,sfn , values)
  local i = {}
  setmetatable(i, GridControl)
  
  i.x = 11
  i.y = y
  -- be nice to do this without 
  -- passing in set and get fns 
  -- but whatever for the moment
  i.valfn = valfn
  i.sfn = sfn
  if #values ~= 6 then
    error("length of values table must be 6")
  end
  i.values = values
  return i
end

function GridControl:draw(g)
  c = {}
  -- v = params:get(self.id)
  v = self.valfn()
  for i=1,6 do 
    if self.y % 2 == 0 then 
      c[i] = 0
    else
      c[i] = 2
    end
    if i == 1 then 
      a = 0
      b = self.values[i]
    elseif i == 7 then 
      a = self.values[i-1]
      b = v
    else
      a = self.values[i-1]
      b = self.values[i]
    end
    if v >= a and v <= b then 
      d = b - a 
      c[i] = math.floor(((v - a)/d) * 15)
      c[i-1] = math.floor(((b -v)/d) * 15)
    end
  end
  for i=0,5 do
    g:led(self.x + i,self.y  , c[i+1])
  end
end

function GridControl:key(x,y,z)
  if y == self.y and x >= self.x and z == 1 then 
    v = (x - self.x) + 1
    self.sfn(self.values[v])
  end
end


return GridControl