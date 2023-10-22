--- LooperControl
-- @module lib.LooperControl

local LooperControl = {}
LooperControl.__index = LooperControl

local fadevalues = {-9,-3,-1,1}


function LooperControl.new(y,id)
  local i = {}
  setmetatable(i, LooperControl)
  -- NOTE - this is assumed to be 
  -- a strip in columns 11+ of the grid
  -- will need some extra code to just place
  -- it anywhere
  i.x = 11
  i.y = y
  i.loopid = id
  i.state = 0
  i.fade = 3
  i.buttons = {0,0} 
  return i
end

function LooperControl:draw(g)
  for i=0,5 do
    if i < 2 then 
      g:led(self.x + i,self.y  , math.floor(self.buttons[i+1] * 15 ))
    else
      c = 0;
		  if ( i - 1 == self.fade ) then 
		    c = 15 
		  end
			g:led(self.x + i ,self.y,c)
    end 
  end
end

function LooperControl:key(x,y,z)
  if y == self.y and x >= self.x then 
    k = x - self.x 
    if k == 0  and z == 1 then 
      if self.state == 0 then 
        self.buttons = {1,1}
        self.state = 1
        engine.record(self.loopid)
      elseif self.state == 1 then 
        self.buttons = {0.5,1}
        self.state = 2
        engine.overdub(self.loopid,fadevalues[self.fade])
      elseif self.state == 2 then 
        self.buttons = {0,0}
        self.state = 0
        engine.reset(self.loopid )
      end
    elseif k == 1 and z == 1 then 
      if self.buttons[2] == 0 then
        self.buttons[2] = 1
        engine.recording(self.loopid,1)
      elseif self.buttons[2] == 1 then
        self.buttons[2] = 0
        engine.recording(self.loopid,0)
      end
    elseif k > 1 and z == 1 then 
      fx = k - 1
			if fx > 0 then 
			  self.fade = fx
				engine.fadelevel(self.loopid,fadevalues[self.fade])				
			end
    end 
  end
end


return LooperControl
